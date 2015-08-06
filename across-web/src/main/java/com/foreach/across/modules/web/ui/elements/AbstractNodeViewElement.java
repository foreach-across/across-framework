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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.elements.support.CssClassAttributeUtils;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for a {@link HtmlViewElement} that supports child nodes.
 *
 * @author Arne Vandamme
 * @see NodeViewElement
 * @see AbstractVoidNodeViewElement
 */
public abstract class AbstractNodeViewElement extends ContainerViewElement implements HtmlViewElement
{
	private Map<String, Object> attributes = new HashMap<>();

	private String tagName, htmlId;

	protected AbstractNodeViewElement( String tagName ) {
		setElementType( StandardViewElements.NODE );
		setTagName( tagName );
	}

	public String getTagName() {
		return tagName;
	}

	protected void setTagName( String tagName ) {
		Assert.notNull( tagName );
		this.tagName = tagName;
	}

	@Override
	public String getHtmlId() {
		return htmlId;
	}

	@Override
	public void setHtmlId( String htmlId ) {
		this.htmlId = htmlId;
	}

	public void addCssClass( String... cssClass ) {
		CssClassAttributeUtils.addCssClass( attributes, cssClass );
	}

	public boolean hasCssClass( String cssClass ) {
		return CssClassAttributeUtils.hasCssClass( attributes, cssClass );
	}

	public void removeCssClass( String... cssClass ) {
		CssClassAttributeUtils.removeCssClass( attributes, cssClass );
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes( Map<String, Object> attributes ) {
		Assert.notNull( attributes );
		this.attributes = attributes;
	}

	public void setAttribute( String attributeName, Object attributeValue ) {
		attributes.put( attributeName, attributeValue );
	}

	public void addAttributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
	}

	public void removeAttribute( String attributeName ) {
		attributes.remove( attributeName );
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
