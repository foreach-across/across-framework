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
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.NestableAttributeHolderNode;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.fragment.StandardFragment;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 */
public class ViewElementElementProcessor
		extends AbstractMarkupSubstitutionElementProcessor implements ViewElementNodeFactory
{
	public static final String ELEMENT_NAME = "view";

	private static final String ATTRIBUTE_ITEM = "element";

	private final ObjectMapper objectMapper = new ObjectMapper();

	public ViewElementElementProcessor() {
		super( ELEMENT_NAME );
	}

	@Override
	protected List<Node> getMarkupSubstitutes( Arguments arguments, Element element ) {
		ViewElement viewElement = retrieveViewElementFromAttribute( arguments, element );

		return buildNodes( viewElement, arguments );
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Node> buildNodes( ViewElement viewElement, Arguments arguments ) {
		if ( hasCustomTemplate( viewElement ) ) {
			return renderCustomTemplate( viewElement, arguments );
		}
		else {
			ViewElementThymeleafBuilder processor = findElementProcessor( viewElement, arguments );

			if ( processor != null ) {
				return processor.buildNodes( viewElement, arguments, this );
			}
		}

		throw new IllegalArgumentException( "Unable to render ViewElement of type " + viewElement.getClass() );
	}

	@Override
	public void setAttribute( NestableAttributeHolderNode node, String attributeName, Object value ) {
		if ( !"class".equals( attributeName ) ) {
			if ( value == null ) {
				node.removeAttribute( attributeName );
			}
			else {
				node.setAttribute( attributeName, serialize( value ) );
			}
		}
		else {
			attributeAppend( node, attributeName, Objects.toString( value ) );
		}
	}

	private void attributeAppend( NestableAttributeHolderNode element, String attributeName, String value ) {
		if ( value != null ) {
			String attributeValue = element.getAttributeValue( attributeName );

			if ( StringUtils.isNotBlank( attributeValue ) ) {
				attributeValue += " " + value;
			}
			else {
				attributeValue = value;
			}

			element.setAttribute( attributeName, attributeValue );
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
	public void setAttributes( NestableAttributeHolderNode node, Map<String, Object> attributes ) {
		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			setAttribute( node, attribute.getKey(), attribute.getValue() );
		}
	}

	private ViewElementThymeleafBuilder findElementProcessor( ViewElement viewElement, Arguments arguments ) {
		ApplicationContext appCtx = ( (SpringWebContext) arguments.getContext() ).getApplicationContext();
		ViewElementNodeBuilderRegistry registry = appCtx.getBean( ViewElementNodeBuilderRegistry.class );

		return registry.getNodeBuilder( viewElement );
	}

	private List<Node> renderCustomTemplate( ViewElement viewElement, Arguments arguments ) {
		Arguments newArguments = arguments.addLocalVariables(
				Collections.singletonMap( "component", (Object) viewElement )
		);

		StandardFragment fragment = StandardFragmentProcessor.computeStandardFragmentSpec(
				newArguments.getConfiguration(),
				newArguments,
				appendFragmentIfRequired( viewElement.getCustomTemplate() ),
				"th", "fragment" );

		return fragment.extractFragment( newArguments.getConfiguration(), newArguments,
		                                 newArguments.getTemplateRepository() );
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

	private ViewElement retrieveViewElementFromAttribute( Arguments arguments, Element element ) {
		String expr = element.getAttributeValue( ATTRIBUTE_ITEM );
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser( arguments.getConfiguration() );
		IStandardExpression expression = parser.parseExpression( arguments.getConfiguration(), arguments, expr );

		Object viewElement = expression.execute( arguments.getConfiguration(), arguments );

		if ( viewElement instanceof ViewElement ) {
			return (ViewElement) viewElement;
		}

		throw new IllegalArgumentException(
				ELEMENT_NAME + " element requires a " + ATTRIBUTE_ITEM + " attribute of type ViewElement"
		);
	}

	@Override
	public int getPrecedence() {
		return 1000;
	}
}
