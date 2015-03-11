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

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for a node element that supports child nodes.
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

	@SuppressWarnings("unchecked")
	public <V> V getAttribute( String attributeName ) {
		return (V) attributes.get( attributeName );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}
}
