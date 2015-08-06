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

/**
 * A void node is a node that has attributes but contains no children.
 *
 * @author Arne Vandamme
 * @see NodeViewElement
 */
public class VoidNodeViewElement extends AbstractVoidNodeViewElement
{
	public VoidNodeViewElement( String name, String tagName ) {
		this( tagName );
		setName( name );
	}

	public VoidNodeViewElement( String tagName ) {
		super( tagName );
	}

	@Override
	public void setTagName( String tagName ) {
		super.setTagName( tagName );
	}
}
