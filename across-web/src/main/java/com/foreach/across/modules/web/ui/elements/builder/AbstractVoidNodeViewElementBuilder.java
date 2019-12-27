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

import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.AbstractVoidNodeViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;

import java.util.HashMap;
import java.util.Map;

/**
 * A base builder for a {@link com.foreach.across.modules.web.ui.elements.AbstractVoidNodeViewElement}.
 *
 * @see AbstractNodeViewElementBuilder
 */
public abstract class AbstractVoidNodeViewElementBuilder<T extends AbstractVoidNodeViewElement, SELF extends AbstractVoidNodeViewElementBuilder<T, SELF>>
		extends ViewElementBuilderSupport<T, SELF>
		implements HtmlViewElementBuilder<T, SELF>
{
	private String htmlId;
	private Map<String, Object> attributes = new HashMap<>();

	@SuppressWarnings("unchecked")
	public SELF htmlId( String htmlId ) {
		this.htmlId = htmlId;
		return (SELF) this;
	}

	public SELF css( String... cssClasses ) {
		return with( HtmlViewElement.Functions.css( cssClasses ) );
	}

	public SELF removeCss( String... cssClasses ) {
		return with( MutableViewElement.Functions.remove( HtmlViewElement.Functions.css( cssClasses ) ) );
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
	protected T apply( T viewElement, ViewElementBuilderContext builderContext ) {
		T element = super.apply( viewElement, builderContext );

		if ( htmlId != null ) {
			element.setHtmlId( htmlId );
		}

		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			element.setAttribute( attribute.getKey(), attribute.getValue() );
		}

		return element;
	}
}
