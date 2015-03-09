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

import com.foreach.across.modules.web.ui.ViewElementSupport;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single configurable node that supports attributes but no child nodes.
 * Allows the tagName of the node to be set.
 *
 * @author Arne Vandamme
 */
public abstract class SingleNodeViewElementSupport extends ViewElementSupport
{
	private Map<String, Object> attributes = new HashMap<>();

	private String tagName;

	protected SingleNodeViewElementSupport( String elementType, String tagName ) {
		super( elementType );
		setTagName( tagName );
	}

	protected String getTagName() {
		return tagName;
	}

	protected void setTagName( String tagName ) {
		Assert.notNull( tagName );
		this.tagName = tagName;
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
