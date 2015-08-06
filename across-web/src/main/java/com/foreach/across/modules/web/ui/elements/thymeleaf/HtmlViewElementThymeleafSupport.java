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
import com.foreach.across.modules.web.thymeleaf.ProcessableAttrProcessor;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;

import java.util.Collections;
import java.util.List;

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
	public List<Node> buildNodes( T viewElement, Arguments arguments, ViewElementNodeFactory viewElementNodeFactory ) {
		Element node = createNode( viewElement, arguments, viewElementNodeFactory );
		ProcessableAttrProcessor.makeProcessable( node );

		attribute( node, "id", retrieveHtmlId( arguments, viewElement ) );

		applyProperties( viewElement, arguments, node );

		viewElementNodeFactory.setAttributes( node, viewElement.getAttributes() );

		if ( viewElement instanceof AbstractNodeViewElement ) {
			for ( ViewElement child : (AbstractNodeViewElement) viewElement ) {
				for ( Node childNode : viewElementNodeFactory.buildNodes( child, arguments ) ) {
					node.addChild( childNode );
				}
			}
		}

		node = postProcess( node, viewElement, arguments, viewElementNodeFactory );

		return Collections.singletonList( (Node) node );
	}

	protected abstract Element createNode( T control,
	                                       Arguments arguments,
	                                       ViewElementNodeFactory viewElementNodeFactory );

	/**
	 * Adapter method, meant for subclass hierarchies.
	 */
	protected void applyProperties( T control, Arguments arguments, Element node ) {
	}

	/**
	 * Adapter method allowing modifying the element (eg wrapping it) after it has been built.
	 */
	protected Element postProcess( Element element,
	                               T control,
	                               Arguments arguments,
	                               ViewElementNodeFactory viewElementNodeFactory ) {
		return element;
	}

	/**
	 * Create a new {@link Element} that is processable and has the given tag name.
	 *
	 * @param tagName value
	 * @return Element that is processable
	 */
	protected Element createElement( String tagName ) {
		Element element = new Element( tagName );
		ProcessableAttrProcessor.makeProcessable( element );

		return element;
	}

	protected void text( Element element, String text ) {
		if ( text != null ) {
			element.addChild( new Text( text ) );
		}
	}

	protected String retrieveHtmlId( Arguments arguments, HtmlViewElement control ) {
		HtmlIdStore idStore = (HtmlIdStore) arguments.getExpressionObjects().get( AcrossWebDialect.HTML_ID_STORE );

		return idStore.retrieveHtmlId( control );
	}

	protected void attribute( Element element,
	                          String attributeName,
	                          Object value,
	                          ViewElementNodeFactory viewElementNodeFactory ) {
		if ( value != null ) {
			viewElementNodeFactory.setAttribute( element, attributeName, value );
		}
	}

	protected void attribute( Element element, String attributeName, String value ) {
		if ( value != null ) {
			element.setAttribute( attributeName, value );
		}
	}

	protected void attributeAppend( Element element, String attributeName, String value ) {
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

	protected void attribute( Element element, String attributeName, boolean condition ) {
		if ( condition ) {
			element.setAttribute( attributeName, attributeName );
		}
	}

	/**
	 * Will append the nodes generated for the child {@link ViewElement} as children to the root {@link Element} passed.
	 * If the child element is null, nothing will be added but no exception will be thrown.
	 *
	 * @param element                to which to add generated child nodes
	 * @param child                  viewelement for which nodes should be generated (can be null)
	 * @param arguments              contextual arguments
	 * @param viewElementNodeFactory root factory for generating the child nodes
	 */
	@SuppressWarnings("unused")
	protected void addChild( Element element,
	                         ViewElement child,
	                         Arguments arguments,
	                         ViewElementNodeFactory viewElementNodeFactory ) {
		if ( child != null ) {
			for ( Node childNode : viewElementNodeFactory.buildNodes( child, arguments ) ) {
				element.addChild( childNode );
			}
		}
	}
}
