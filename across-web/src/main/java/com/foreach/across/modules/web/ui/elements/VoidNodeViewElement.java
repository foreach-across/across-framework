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

import lombok.NonNull;

import java.util.Map;

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
	public VoidNodeViewElement setTagName( @NonNull String tagName ) {
		return (VoidNodeViewElement) super.setTagName( tagName );
	}

	@Override
	public VoidNodeViewElement setHtmlId( String htmlId ) {
		return (VoidNodeViewElement) super.setHtmlId( htmlId );
	}

	@Override
	public VoidNodeViewElement addCssClass( String... cssClass ) {
		return (VoidNodeViewElement) super.addCssClass( cssClass );
	}

	@Override
	public VoidNodeViewElement removeCssClass( String... cssClass ) {
		return (VoidNodeViewElement) super.removeCssClass( cssClass );
	}

	@Override
	public VoidNodeViewElement setAttributes( @NonNull Map<String, Object> attributes ) {
		return (VoidNodeViewElement) super.setAttributes( attributes );
	}

	@Override
	public VoidNodeViewElement setAttribute( String attributeName, Object attributeValue ) {
		return (VoidNodeViewElement) super.setAttribute( attributeName, attributeValue );
	}

	@Override
	public VoidNodeViewElement addAttributes( Map<String, Object> attributes ) {
		return (VoidNodeViewElement) super.addAttributes( attributes );
	}

	@Override
	public VoidNodeViewElement removeAttribute( String attributeName ) {
		return (VoidNodeViewElement) super.removeAttribute( attributeName );
	}

	@Override
	public VoidNodeViewElement setName( String name ) {
		return (VoidNodeViewElement) super.setName( name );
	}

	@Override
	public VoidNodeViewElement setCustomTemplate( String customTemplate ) {
		return (VoidNodeViewElement) super.setCustomTemplate( customTemplate );
	}

	@Override
	protected VoidNodeViewElement setElementType( String elementType ) {
		return (VoidNodeViewElement) super.setElementType( elementType );
	}

	@Override
	public VoidNodeViewElement set( WitherSetter... setters ) {
		super.set( setters );
		return this;
	}

	@Override
	public VoidNodeViewElement remove( WitherRemover... functions ) {
		super.remove( functions );
		return this;
	}
}
