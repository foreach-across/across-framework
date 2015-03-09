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

/**
 * Represents a generic node (html element).  Supports tag, set of attributes and child elements.
 *
 * @author Arne Vandamme
 */
public class NodeViewElement extends NodeViewElementSupport
{
	public static final String TYPE = StandardViewElements.NODE;

	private String tagName;

	public NodeViewElement() {
		super( TYPE );
	}

	public NodeViewElement( String name ) {
		super( TYPE );
		setName( name );
	}

	public NodeViewElement( String name, String tag ) {
		super( TYPE );
		setName( name );
		setTagName( tag );
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName( String tagName ) {
		this.tagName = tagName;
	}

	public static NodeViewElement forTag( String tagName ) {
		NodeViewElement node = new NodeViewElement();
		node.setTagName( tagName );

		return node;
	}
}
