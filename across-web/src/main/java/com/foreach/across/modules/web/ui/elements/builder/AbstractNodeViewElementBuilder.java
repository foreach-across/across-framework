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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.support.CssClassAttributeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A base builder for a {@link AbstractNodeViewElement}.
 *
 * @see AbstractVoidNodeViewElementBuilder
 */
public abstract class AbstractNodeViewElementBuilder<T extends AbstractNodeViewElement, SELF extends AbstractNodeViewElementBuilder<T, SELF>>
		extends ContainerViewElementBuilderSupport<T, SELF>
		implements HtmlViewElementBuilder<T, SELF>
{
	private String htmlId;
	private Map<String, Object> attributes = new HashMap<>();

	@SuppressWarnings("unchecked")
	public SELF htmlId( String htmlId ) {
		this.htmlId = htmlId;
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF css( String... cssClasses ) {
		CssClassAttributeUtils.addCssClass( attributes, cssClasses );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF removeCss( String... cssClasses ) {
		CssClassAttributeUtils.removeCssClass( attributes, cssClasses );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF attribute( String name, Object value ) {
		attributes.put( name, value );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF attributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF removeAttribute( String name ) {
		attributes.put( name, null );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF clearAttributes() {
		attributes.clear();
		return (SELF) this;
	}

	@Override
	protected T apply( T element, ViewElementBuilderContext builderContext ) {
		if ( htmlId != null ) {
			element.setHtmlId( htmlId );
		}

		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			element.setAttribute( attribute.getKey(), attribute.getValue() );
		}

		return super.apply( element, builderContext );
	}
}
