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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for a node element that supports child nodes and attributes.  One of the attributes is
 * the html class and has several shortcut methods.
 *
 * @author Arne Vandamme
 */
public abstract class NodeViewElementSupport extends ContainerViewElement
{
	private String htmlId;
	private Map<String, Object> attributes = new HashMap<>();

	protected NodeViewElementSupport( String elementType ) {
		setElementType( elementType );
	}

	public String getHtmlId() {
		return htmlId;
	}

	public void setHtmlId( String htmlId ) {
		this.htmlId = htmlId;
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

	public Object getAttribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	public void addCssClass( String... cssClass ) {
		setAttribute(
				"class",
				StringUtils.join(
						ArrayUtils.addAll( ArrayUtils.removeElements( cssClasses(), cssClass ), cssClass ), " "
				)
		);
	}

	public boolean hasCssClass( String cssClass ) {
		return ArrayUtils.contains( cssClasses(), cssClass );
	}

	public void removeCssClass( String... cssClass ) {
		setAttribute( "class", StringUtils.join( ArrayUtils.removeElements( cssClasses(), cssClass ) ) );
	}

	private String[] cssClasses() {
		String css = StringUtils.defaultString( getAttribute( "class", String.class ) );
		return StringUtils.split( css, " " );
	}

	/**
	 * Get the attribute value if it is of the expected type.  If the attribute is of a different type,
	 * it will not be returned.
	 *
	 * @param attributeName name of the attribute
	 * @param expectedType  for the attribute
	 * @param <V>           type
	 * @return attribute value or {@code null} if not available or not of the expected type
	 */
	public <V> V getAttribute( String attributeName, Class<V> expectedType ) {
		return returnIfType( getAttribute( attributeName ), expectedType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	/**
	 * Helper that returns the value only if it is of the expected type.  Else {@code null} is returned.
	 */
	@SuppressWarnings("unchecked")
	protected <V> V returnIfType( Object value, Class<V> elementType ) {
		return elementType.isInstance( value ) ? (V) value : null;
	}
}
