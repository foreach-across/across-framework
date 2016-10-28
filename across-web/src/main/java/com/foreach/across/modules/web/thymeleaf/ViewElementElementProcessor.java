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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.Fragment;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Map;
import java.util.Objects;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 */
public class ViewElementElementProcessor
		extends AbstractElementTagProcessor implements ViewElementNodeFactory
{
	public static final String ELEMENT_NAME = "view";

	private static final String ATTRIBUTE_ITEM = "element";

	private final ObjectMapper objectMapper = new ObjectMapper();

	public ViewElementElementProcessor() {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				AcrossWebDialect.PREFIX,     // Prefix to be applied to name for matching
				ELEMENT_NAME,          // Tag name: match specifically this tag
				true,              // Apply dialect prefix to tag name
				null,              // No attribute name: will match by tag name
				false,             // No prefix to be applied to attribute name
				10000 );       // Precedence (inside dialect's own precedence)
	}

	//	@Override
//	protected List<Node> getMarkupSubstitutes( ITemplateContext context, IProcessableElementTag element ) {
//		ViewElement viewElement = retrieveViewElementFromAttribute( context, element );
//
//		return buildModel( viewElement, context );
//	}
//
	@Override
	@SuppressWarnings("unchecked")
	public ProcessableModel buildModel( ViewElement viewElement, ITemplateContext context ) {
		if ( hasCustomTemplate( viewElement ) ) {
			return renderCustomTemplate( viewElement, context );
		}
		else {
			ViewElementThymeleafBuilder processor = findElementProcessor( viewElement, context );

			if ( processor != null ) {
				return processor.buildModel( viewElement, context, this );
			}
		}

		throw new IllegalArgumentException( "Unable to render ViewElement of type " + viewElement.getClass() );
	}

	@Override
	public void setAttribute( Map<String, String> nodeAttributes, String attributeName, Object value ) {
		if ( !"class".equals( attributeName ) ) {
			if ( value == null ) {
				nodeAttributes.remove( attributeName );
			}
			else {
				nodeAttributes.put( attributeName, serialize( value ) );
			}
		}
		else {
			attributeAppend( nodeAttributes, attributeName, Objects.toString( value ) );
		}
	}

	private void attributeAppend( Map<String, String> nodeAttributes, String attributeName, String value ) {
		if ( value != null ) {
			String attributeValue = nodeAttributes.get( attributeName );

			if ( StringUtils.isNotBlank( attributeValue ) ) {
				attributeValue += " " + value;
			}
			else {
				attributeValue = value;
			}

			nodeAttributes.put( attributeName, attributeValue );
		}
	}

	private String serialize( Object value ) {
		if ( value instanceof String || ClassUtils.isPrimitiveOrWrapper( value.getClass() ) ) {
			return Objects.toString( value );
		}

		try {
			return objectMapper.writeValueAsString( value );
		}
		catch ( JsonProcessingException jpe ) {
			throw new RuntimeException( jpe );
		}
	}

	@Override
	public void setAttributes( Map<String, String> nodeAttributes, Map<String, Object> attributes ) {
		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			setAttribute( nodeAttributes, attribute.getKey(), attribute.getValue() );
		}
	}

	private ViewElementThymeleafBuilder findElementProcessor( ViewElement viewElement, ITemplateContext context ) {
		ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext(
				( (WebEngineContext) context ).getRequest() );
		ViewElementNodeBuilderRegistry registry = appCtx.getBean( ViewElementNodeBuilderRegistry.class );

		return registry.getNodeBuilder( viewElement );
	}

	private ProcessableModel renderCustomTemplate( ViewElement viewElement, ITemplateContext context ) {
		( (WebEngineContext) context ).setVariable( "component", viewElement );

		String templateWithFragment = appendFragmentIfRequired( viewElement.getCustomTemplate() );

		IModelFactory modelFactory = context.getModelFactory();
		IModel model = modelFactory.createModel();

		model.add( modelFactory.createOpenElementTag( "div", "th:replace", templateWithFragment, false ) );
		model.add( modelFactory.createCloseElementTag( "div" ) );

		final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(
				context.getConfiguration() );
		IStandardExpression expression = expressionParser.parseExpression( context, "~{" + templateWithFragment + "}" );
		Object result = expression.execute( context );
		return new ProcessableModel( ( (Fragment) result ).getTemplateModel(), true );
	}

	/**
	 * Append the fragment to the custom template name if there is no fragment.
	 */
	private String appendFragmentIfRequired( String customTemplate ) {
		if ( !StringUtils.contains( customTemplate, "::" ) ) {
			return customTemplate + " :: render(${component})";
		}

		return customTemplate;
	}

	private boolean hasCustomTemplate( ViewElement viewElement ) {
		return viewElement.getCustomTemplate() != null;
	}

	private ViewElement retrieveViewElementFromAttribute( ITemplateContext context, IProcessableElementTag element ) {
		String expr = element.getAttributeValue( ATTRIBUTE_ITEM );
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser( context.getConfiguration() );
		IStandardExpression expression = parser.parseExpression( context, expr );

		Object viewElement = expression.execute( context );

		if ( viewElement instanceof ViewElement ) {
			return (ViewElement) viewElement;
		}

		throw new IllegalArgumentException(
				ELEMENT_NAME + " element requires a " + ATTRIBUTE_ITEM + " attribute of type ViewElement"
		);
	}

	@Override
	protected void doProcess( ITemplateContext context,
	                          IProcessableElementTag tag,
	                          IElementTagStructureHandler structureHandler ) {

		ViewElement viewElement = retrieveViewElementFromAttribute( context, tag );
		ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext(
				( (WebEngineContext) context ).getRequest() );
		ViewElementNodeBuilderRegistry registry = appCtx.getBean( ViewElementNodeBuilderRegistry.class );

		ThymeleafModelBuilder builder = new ThymeleafModelBuilder( context, registry );
		builder.addViewElement( viewElement );
		structureHandler.replaceWith( builder.createModel(), true );
		/*
		ProcessableModel model = buildModel( viewElement, context );

		structureHandler.replaceWith( model.getModel(), model.isProcessable() );*/
	}
}
