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
package com.foreach.across.modules.web.ui.elements.thymeleaf;

import com.foreach.across.modules.web.thymeleaf.AcrossWebDialect;
import com.foreach.across.modules.web.thymeleaf.HtmlIdStore;
import com.foreach.across.modules.web.thymeleaf.ProcessableModel;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Supports implementations of {@link com.foreach.across.modules.web.ui.elements.AbstractVoidNodeViewElement}
 * and {@link AbstractNodeViewElement}.
 *
 * @author Arne Vandamme
 */
public abstract class HtmlViewElementThymeleafSupport<T extends HtmlViewElement>
		implements ViewElementThymeleafBuilder<T>
{
	@Override
	public ProcessableModel buildModel( T viewElement,
	                                    ITemplateContext context,
	                                    ViewElementNodeFactory viewElementNodeFactory ) {
		Map<String, String> nodeAttributes = new HashMap<>();
		attribute( nodeAttributes, "id", retrieveHtmlId( context, viewElement ) );
		applyProperties( viewElement, context, nodeAttributes );
		viewElementNodeFactory.setAttributes( nodeAttributes, viewElement.getAttributes() );
		ProcessableModel model = createNode( viewElement, context, viewElementNodeFactory, nodeAttributes );

		if ( viewElement instanceof AbstractNodeViewElement ) {
			for ( ViewElement child : ( (AbstractNodeViewElement) viewElement ).getChildren() ) {
				ProcessableModel childModel = viewElementNodeFactory.buildModel( child, context );
				model.getModel().addModel( childModel.getModel() );
			}
		}

		model.getModel().add( context.getModelFactory().createCloseElementTag( viewElement.getTagName() ) );

		//TODO: TH3
		//node = postProcess( node, viewElement, context, viewElementNodeFactory );

		return model;
	}

	protected abstract ProcessableModel createNode( T control,
	                                                ITemplateContext arguments,
	                                                ViewElementNodeFactory viewElementNodeFactory,
	                                                Map<String, String> nodeAttributes );

	/**
	 * Adapter method, meant for subclass hierarchies.
	 */
	protected void applyProperties( T control, ITemplateContext arguments, Map<String, String> nodeAttributes ) {
	}

	/**
	 * Adapter method allowing modifying the element (eg wrapping it) after it has been built.
	 */
//	protected Element postProcess( Element element,
//	                               T control,
//	                               Arguments arguments,
//	                               ViewElementNodeFactory viewElementNodeFactory ) {
//		return element;
//	}

	/**
	 * Create a new {@link ProcessableModel} that is processable and has the given tag name.
	 *
	 * @param tagName    value
	 * @param attributes
	 * @return Element that is processable
	 */
	protected IOpenElementTag createElement( IModelFactory modelFactory,
	                                         String tagName,
	                                         Map<String, String> attributes ) {
		return modelFactory.createOpenElementTag( tagName, attributes, AttributeValueQuotes.SINGLE, false );
	}

	protected void text( IModelFactory modelFactory, IModel model, String text ) {
		if ( text != null ) {
			model.add( modelFactory.createText( text ) );
		}
	}

	protected String retrieveHtmlId( ITemplateContext context, HtmlViewElement control ) {
		HtmlIdStore idStore = (HtmlIdStore) context.getExpressionObjects().getObject( AcrossWebDialect.HTML_ID_STORE );
		return idStore.retrieveHtmlId( context, control );
	}

	protected void attribute( Map<String, String> nodeAttributes,
	                          String attributeName,
	                          Object value,
	                          ViewElementNodeFactory viewElementNodeFactory ) {
		if ( value != null ) {
			viewElementNodeFactory.setAttribute( nodeAttributes, attributeName, value );
		}
	}

	protected void attribute( Map<String, String> nodeAttributes, String attributeName, String value ) {
		if ( value != null ) {
			nodeAttributes.put( attributeName, value );
		}
	}

//	protected void attributeAppend( Element element, String attributeName, String value ) {
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
//	protected void attribute( Element element, String attributeName, boolean condition ) {
//		if ( condition ) {
//			element.setAttribute( attributeName, attributeName );
//		}
//	}
//
//	/**
//	 * Will append the nodes generated for the child {@link ViewElement} as children to the root {@link Element} passed.
//	 * If the child element is null, nothing will be added but no exception will be thrown.
//	 *
//	 * @param element                to which to add generated child nodes
//	 * @param child                  viewelement for which nodes should be generated (can be null)
//	 * @param arguments              contextual arguments
//	 * @param viewElementNodeFactory root factory for generating the child nodes
//	 */
//	@SuppressWarnings("unused")
//	protected void addChild( Element element,
//	                         ViewElement child,
//	                         Arguments arguments,
//	                         ViewElementNodeFactory viewElementNodeFactory ) {
//		if ( child != null ) {
//			for ( Node childNode : viewElementNodeFactory.buildModel( child, arguments ) ) {
//				element.addChild( childNode );
//			}
//		}
//	}
}
