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

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElementSupport;
import com.foreach.across.modules.web.ui.elements.support.CssClassAttributeUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for a single configurable node that supports attributes but no child nodes.
 *
 * @author Arne Vandamme
 * @see VoidNodeViewElement
 * @see AbstractNodeViewElement
 */
@Accessors(chain = true)
public abstract class AbstractVoidNodeViewElement extends ViewElementSupport implements HtmlViewElement
{
	private Map<String, Object> attributes = new HashMap<>();

	@NonNull
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private String tagName;

	@Getter
	@Setter
	private String htmlId;

	protected AbstractVoidNodeViewElement( String tagName ) {
		super( StandardViewElements.NODE );
		setTagName( tagName );
	}

	public HtmlViewElement addCssClass( String... cssClass ) {
		CssClassAttributeUtils.addCssClass( attributes, cssClass );
		return this;
	}

	public boolean hasCssClass( String cssClass ) {
		return CssClassAttributeUtils.hasCssClass( attributes, cssClass );
	}

	public HtmlViewElement removeCssClass( String... cssClass ) {
		CssClassAttributeUtils.removeCssClass( attributes, cssClass );
		return this;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public HtmlViewElement setAttributes( @NonNull Map<String, Object> attributes ) {
		this.attributes = attributes;
		return this;
	}

	public HtmlViewElement setAttribute( String attributeName, Object attributeValue ) {
		attributes.put( attributeName, attributeValue );
		return this;
	}

	public HtmlViewElement addAttributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
		return this;
	}

	public HtmlViewElement removeAttribute( String attributeName ) {
		attributes.remove( attributeName );
		return this;
	}

	@Override
	public Object getAttribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	@Override
	public <V> V getAttribute( String attributeName, Class<V> expectedType ) {
		return returnIfType( attributes.get( attributeName ), expectedType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	@SuppressWarnings("unchecked")
	protected <V> V returnIfType( Object value, Class<V> elementType ) {
		return elementType.isInstance( value ) ? (V) value : null;
	}
}
