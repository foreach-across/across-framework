/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.support.AttributeWitherFunction;
import com.foreach.across.modules.web.ui.elements.support.CssClassWitherFunction;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Map;

/**
 * Adds base properties for a HTML node, like css class and html id support.
 *
 * @author Arne Vandamme
 */
public interface HtmlViewElement extends MutableViewElement
{
	String getTagName();

	HtmlViewElement addCssClass( String... cssClass );

	boolean hasCssClass( String cssClass );

	HtmlViewElement removeCssClass( String... cssClass );

	HtmlViewElement setHtmlId( String id );

	String getHtmlId();

	Map<String, Object> getAttributes();

	HtmlViewElement setAttributes( Map<String, Object> attributes );

	HtmlViewElement setAttribute( String attributeName, Object attributeValue );

	HtmlViewElement addAttributes( Map<String, Object> attributes );

	HtmlViewElement removeAttribute( String attributeName );

	Object getAttribute( String attributeName );

	<V, U extends V> U getAttribute( String attributeName, Class<V> expectedType );

	boolean hasAttribute( String attributeName );

	@Override
	default HtmlViewElement set( WitherSetter... setters ) {
		MutableViewElement.super.set( setters );
		return this;
	}

	@Override
	default HtmlViewElement remove( WitherRemover... functions ) {
		MutableViewElement.super.remove( functions );
		return this;
	}
	/**
	 * Nested interface which contains wither functions specific for HTML view elements.
	 */
	interface Functions
	{
		/**
		 * Set internal {@link #getTagName()} property.
		 */
		static WitherSetter tagName( String name ) {
			return e -> {
				if ( e instanceof AbstractNodeViewElement ) {
					( (AbstractNodeViewElement) e ).setTagName( name );
				}
				else if ( e instanceof AbstractVoidNodeViewElement ) {
					( (AbstractVoidNodeViewElement) e ).setTagName( name );
				}
				else {
					throw new IllegalArgumentException( "Setting tag name only possible on AbstractNodeViewElement or AbstractVoidNodeViewElement" );
				}
			};
		}

		/**
		 * Set internal {@link #getHtmlId()} property.
		 */
		static WitherSetter htmlId( String id ) {
			return e -> ( (HtmlViewElement) e ).setHtmlId( id );
		}

		/**
		 * Configure one or more CSS classes. Can also be used for removal.
		 */
		static CssClassWitherFunction css( String... cssClassNames ) {
			return new CssClassWitherFunction( cssClassNames );
		}

		/**
		 * Configure a {@code data-} attribute value.
		 */
		static AttributeWitherFunction.AttributeValueWitherFunction<Object> data( String attributeName, Object attributeValue ) {
			return data( attributeName ).withValue( attributeValue );
		}

		/**
		 * Configure a {@code data-} attribute value. Can be used for getting the attribute value,
		 * removing the attribute, or setting it using {@link AttributeWitherFunction#withValue(Object)}.
		 */
		static AttributeWitherFunction<Object> data( @NonNull String attributeName ) {
			return attribute( "data-" + attributeName );
		}

		/**
		 * Configure an {@code aria-} attribute value.
		 */
		static AttributeWitherFunction.AttributeValueWitherFunction<Object> aria( String attributeName, Object attributeValue ) {
			return aria( attributeName ).withValue( attributeValue );
		}

		/**
		 * Configure a {@code aria-} attribute value. Can be used for getting the attribute value,
		 * removing the attribute, or setting it using {@link AttributeWitherFunction#withValue(Object)}.
		 */
		static AttributeWitherFunction<Object> aria( @NonNull String attributeName ) {
			return attribute( "aria-" + attributeName );
		}

		/**
		 * Configure a custom attribute value.
		 */
		static AttributeWitherFunction.AttributeValueWitherFunction<Object> attribute( String attributeName, Object attributeValue ) {
			return new AttributeWitherFunction<>( attributeName ).withValue( attributeValue );
		}

		/**
		 * Configure a custom attribute value. Can be used for getting the attribute value,
		 * removing the attribute, or setting it using {@link AttributeWitherFunction#withValue(Object)}.
		 * Can also be used to check for the presence of the attribute (as {@link java.util.function.Predicate}).
		 */
		static AttributeWitherFunction<Object> attribute( String attributeName ) {
			return new AttributeWitherFunction<>( attributeName );
		}

		/**
		 * Add children to a container element.
		 */
		static WitherSetter<ContainerViewElement> children( ViewElement... elements ) {
			return container -> container.addChildren( Arrays.asList( elements ) );
		}
	}
}
