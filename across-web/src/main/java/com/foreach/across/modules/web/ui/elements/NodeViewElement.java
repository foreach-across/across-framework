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
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a generic node (html element).  Supports tag, set of attributes and child elements.
 *
 * @author Arne Vandamme
 */
public class NodeViewElement extends ContainerViewElement
{
	public static final String TYPE = StandardViewElements.NODE;

	private String tagName;
	private Map<String, String> attributes = new HashMap<>();

	public NodeViewElement() {
		setElementType( TYPE );
	}

	public NodeViewElement( String name ) {
		super( name );
	}

	public NodeViewElement( String name, String tag ) {
		super( name );
		setTagName( tag );
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName( String tagName ) {
		this.tagName = tagName;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes( Map<String, String> attributes ) {
		Assert.notNull(attributes);
		this.attributes = attributes;
	}

	public void setAttribute( String attributeName, String attributeValue ) {
		attributes.put( attributeName, attributeValue );
	}

	public void addAttributes( Map<String, String> attributes ) {
		this.attributes.putAll( attributes );
	}

	public void removeAttribute( String attributeName ) {
		attributes.remove( attributeName );
	}

	public String getAttribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	public static NodeViewElement forTag( String tagName ) {
		NodeViewElement node = new NodeViewElement();
		node.setTagName( tagName );

		return node;
	}
}
