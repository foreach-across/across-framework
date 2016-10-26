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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
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
//		return buildNodes( viewElement, context );
//	}
//
	@Override
	@SuppressWarnings("unchecked")
	public IModel buildNodes( ViewElement viewElement, ITemplateContext context ) {
		if ( hasCustomTemplate( viewElement ) ) {
			return renderCustomTemplate( viewElement, context );
		}
		else {
			ViewElementThymeleafBuilder processor = findElementProcessor( viewElement, context );

			if ( processor != null ) {
				return processor.buildNodes( viewElement, context, this );
			}
		}

		throw new IllegalArgumentException( "Unable to render ViewElement of type " + viewElement.getClass() );
	}

	//
//	@Override
//	public void setAttribute( NestableAttributeHolderNode node, String attributeName, Object value ) {
//		if ( !"class".equals( attributeName ) ) {
//			if ( value == null ) {
//				node.removeAttribute( attributeName );
//			}
//			else {
//				node.setAttribute( attributeName, serialize( value ) );
//			}
//		}
//		else {
//			attributeAppend( node, attributeName, Objects.toString( value ) );
//		}
//	}
//
//	private void attributeAppend( NestableAttributeHolderNode element, String attributeName, String value ) {
//		if ( value != null ) {
//			String attributeValue = element.getAttributeValue( attributeName );
//
//			if ( StringUtils.isNotBlank( attributeValue ) ) {
//				attributeValue += " " + value;
//			}
//			else {
//				attributeValue = value;
//			}
//
//			element.setAttribute( attributeName, attributeValue );
//		}
//	}
//
//	private String serialize( Object value ) {
//		if ( value instanceof String || ClassUtils.isPrimitiveOrWrapper( value.getClass() ) ) {
//			return Objects.toString( value );
//		}
//
//		try {
//			return objectMapper.writeValueAsString( value );
//		}
//		catch ( JsonProcessingException jpe ) {
//			throw new RuntimeException( jpe );
//		}
//	}
//
//	@Override
//	public void setAttributes( NestableAttributeHolderNode node, Map<String, Object> attributes ) {
//		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
//			setAttribute( node, attribute.getKey(), attribute.getValue() );
//		}
//	}
//
	private ViewElementThymeleafBuilder findElementProcessor( ViewElement viewElement, ITemplateContext context ) {
		ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext(
				( (WebEngineContext) context ).getRequest() );
		ViewElementNodeBuilderRegistry registry = appCtx.getBean( ViewElementNodeBuilderRegistry.class );

		return registry.getNodeBuilder( viewElement );
	}

	private IModel renderCustomTemplate( ViewElement viewElement, ITemplateContext context ) {
		( (WebEngineContext) context ).setVariable( "component", viewElement );
//		Arguments newArguments = context.addLocalVariables(
//				Collections.singletonMap( "component", viewElement )
//		);

		String templateWithFragment = appendFragmentIfRequired( viewElement.getCustomTemplate() );

//		StandardFragment fragment = StandardFragmentTagProcessor.computeStandardFragmentSpec(
//				newArguments.getConfiguration(),
//				newArguments,
//				templateWithFragment,
//				"th", "fragment" );
//
//		List<Node> nodes = fragment.extractFragment( newArguments.getConfiguration(), newArguments,
//		                                             newArguments.getTemplateRepository() );
//
//		if ( nodes == null ) {
//			throw new TemplateProcessingException( "Not a valid template [" + templateWithFragment + "]" );
//		}

		IModelFactory modelFactory = context.getModelFactory();
		IModel model = modelFactory.createModel();

		model.add( modelFactory.createOpenElementTag( "div", "th:replace", templateWithFragment, false ) );
		model.add( modelFactory.createCloseElementTag( "div" ) );

		//TemplateManager manager = context.getConfiguration().getTemplateManager();
		final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(
				context.getConfiguration() );
		IStandardExpression expression = expressionParser.parseExpression( context, "~{" + templateWithFragment + "}" );
		Object result = expression.execute( context );
//		FragmentSignature fragmentSignature = FragmentSignatureUtils.parseFragmentSignature( context.getConfiguration(), templateWithFragment );
//		final Writer stringWriter = new FastStringWriter( 200);
//		context.getConfiguration().getTemplateManager().process(null, context, stringWriter);
//
//		TemplateModel templateModel = manager.parseStandalone( context, templateWithFragment, Collections.emptySet(), TemplateMode.HTML, true, true );

//		TemplateData templateData = new TemplateData( templateWithFragment, null, context.getTemplateData().getTemplateMode(), context.getTemplateData().getValidity() );
		//model = context.getModelFactory().parse( context.getTemplateData(), "<div th:replace='" + templateWithFragment + "'></div>" );
		return ( (Fragment) result ).getTemplateModel();
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

		IModel model = buildNodes( viewElement, context );

		structureHandler.replaceWith( model, false );
	}
}
