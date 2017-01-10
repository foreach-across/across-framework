/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring4.context.SpringContextUtils;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EscapedAttributeUtils;
import org.thymeleaf.util.Validate;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Attribute processor for across:static or across:resource attributes.
 * Support across:static-name and across:resource-name where [name] defines the resulting attribute name.
 * Otherwise the attribute name will be determined by the tag the attribute is being used on.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
class ResourceAttributeProcessor extends AbstractElementTagProcessor
{
	private final static String[] SRC_ELEMENTS
			= new String[] { "img", "script", "audio", "embed", "iframe", "input", "source", "track", "video" };
	private final static String[] HREF_ELEMENTS
			= new String[] { "a", "area", "base", "link" };
	private final static String[] XLINK_HREF_ELEMENTS
			= new String[] { "image", "use" };

	ResourceAttributeProcessor() {
		super(
				TemplateMode.HTML,              // This processor will apply only to HTML mode
				AcrossWebDialect.PREFIX,        // Prefix to be applied to name for matching
				null,               // Tag name: match specifically this tag
				false,          // Apply dialect prefix to tag name
				null,              // No attribute name: will match by tag name
				true,          // No prefix to be applied to attribute name
				10000
		);
	}

	@Override
	protected final void doProcess(
			final ITemplateContext context,
			final IProcessableElementTag tag,
			final IElementTagStructureHandler structureHandler ) {
		Arrays.stream( tag.getAllAttributes() )
		      .filter( a -> isStaticOrResourceAttribute( a.getAttributeDefinition().getAttributeName() ) )
		      .forEach( attribute -> {
			      handleAttribute( context, tag, structureHandler, attribute );
		      } );
	}

	private void handleAttribute( ITemplateContext context,
	                              IProcessableElementTag tag,
	                              IElementTagStructureHandler structureHandler,
	                              IAttribute attribute ) {
		String completeAttributeName = attribute.getAttributeCompleteName();

		try {
			String attributeName = attribute.getAttributeDefinition().getAttributeName().getAttributeName();

			final IEngineConfiguration configuration = context.getConfiguration();
			final IStandardExpressionParser parser = StandardExpressions.getExpressionParser( configuration );
			String attributeValue = EscapedAttributeUtils.unescapeAttribute(
					context.getTemplateMode(), tag.getAttributeValue( completeAttributeName )
			);

			if ( StringUtils.startsWithAny( attributeValue, "${", "#{", "@{" ) ) {
				IStandardExpression expression = parser.parseExpression( context, attributeValue );
				attributeValue = (String) expression.execute( context );
			}

			String targetAttributeName = determineTargetAttributeName( attributeName, tag );
			Validate.notNull( targetAttributeName, "Could not determine target attribute name" );

			PrefixingPathContext pathContext = retrievePathContext( context, attributeName );
			attributeValue = processLink( context, pathContext.path( attributeValue ) );

			structureHandler.setAttribute( targetAttributeName, attributeValue );

			structureHandler.removeAttribute( completeAttributeName );
		}
		catch ( final TemplateProcessingException e ) {
			// This is a nice moment to check whether the execution raised an error and, if so, add location information
			// Note this is similar to what is done at the superclass AbstractElementTagProcessor, but we can be more
			// specific because we know exactly what attribute was being executed and caused the error
			if ( tag.hasLocation() ) {
				if ( !e.hasTemplateName() ) {
					e.setTemplateName( tag.getTemplateName() );
				}
				if ( !e.hasLineAndCol() ) {
					if ( completeAttributeName == null ) {
						// We don't have info about the specific attribute provoking the error
						e.setLineAndCol( tag.getLine(), tag.getCol() );
					}
					else {
						e.setLineAndCol( attribute.getLine(), attribute.getCol() );
					}
				}
			}
			throw e;
		}
		catch ( final Exception e ) {
			int line = tag.getLine();
			int col = tag.getCol();
			if ( completeAttributeName != null ) {
				// We don't have info about the specific attribute provoking the error
				line = attribute.getLine();
				col = attribute.getCol();
			}
			throw new TemplateProcessingException(
					"Error during execution of processor '" + this.getClass().getName() + "'",
					tag.getTemplateName(), line, col, e );
		}
	}

	private PrefixingPathContext retrievePathContext( ITemplateContext context, String attributeName ) {
		final ApplicationContext appCtx = SpringContextUtils.getApplicationContext( context );
		PrefixingPathRegistry pathRegistry = appCtx.getBean( PrefixingPathRegistry.class );

		if ( StringUtils.startsWith( attributeName, "static" ) ) {
			return pathRegistry.get( "static" );
		}

		return pathRegistry.get( "resource" );
	}

	private String determineTargetAttributeName( String attributeName, IProcessableElementTag tag ) {
		int ix = attributeName.indexOf( '-' );

		if ( ix >= 0 ) {
			return StringUtils.replace( attributeName.substring( ix + 1 ), "_", ":" );
		}

		for ( String candidate : HREF_ELEMENTS ) {
			if ( StringUtils.equalsIgnoreCase( candidate, tag.getElementCompleteName() ) ) {
				return "href";
			}
		}

		for ( String candidate : SRC_ELEMENTS ) {
			if ( StringUtils.equalsIgnoreCase( candidate, tag.getElementCompleteName() ) ) {
				return "src";
			}
		}

		for ( String candidate : XLINK_HREF_ELEMENTS ) {
			if ( StringUtils.equalsIgnoreCase( candidate, tag.getElementCompleteName() ) ) {
				return "xlink:href";
			}
		}

		return null;
	}

	private String processLink( final IExpressionContext context, final String link ) {

		if ( !( context instanceof IWebContext ) ) {
			return link;
		}

		final HttpServletResponse response = ( (IWebContext) context ).getResponse();
		return ( response != null ? response.encodeURL( link ) : link );

	}

	private boolean isStaticOrResourceAttribute( AttributeName attributeName ) {
		return AcrossWebDialect.PREFIX.equals( attributeName.getPrefix() )
				&& StringUtils.startsWithAny( attributeName.getAttributeName(), "static", "resource" );
	}
}
