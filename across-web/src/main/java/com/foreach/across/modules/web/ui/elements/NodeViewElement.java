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

import com.foreach.across.modules.web.ui.ViewElement;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a generic node (html element).  Supports tag, set of attributes and child elements.
 *
 * @author Arne Vandamme
 */
public class NodeViewElement extends AbstractNodeViewElement
{
	public NodeViewElement( String name, String tagName ) {
		this( tagName );
		setName( name );
	}

	public NodeViewElement( String tagName ) {
		super( tagName );
	}

	@Override
	public NodeViewElement setTagName( String tagName ) {
		return (NodeViewElement) super.setTagName( tagName );
	}

	@Override
	public NodeViewElement setHtmlId( String htmlId ) {
		return (NodeViewElement) super.setHtmlId( htmlId );
	}

	@Override
	public NodeViewElement addCssClass( String... cssClass ) {
		return (NodeViewElement) super.addCssClass( cssClass );
	}

	@Override
	public NodeViewElement removeCssClass( String... cssClass ) {
		return (NodeViewElement) super.removeCssClass( cssClass );
	}

	@Override
	public NodeViewElement setAttributes( @NonNull Map<String, Object> attributes ) {
		return (NodeViewElement) super.setAttributes( attributes );
	}

	@Override
	public NodeViewElement setAttribute( String attributeName, Object attributeValue ) {
		return (NodeViewElement) super.setAttribute( attributeName, attributeValue );
	}

	@Override
	public NodeViewElement addAttributes( Map<String, Object> attributes ) {
		return (NodeViewElement) super.addAttributes( attributes );
	}

	@Override
	public NodeViewElement removeAttribute( String attributeName ) {
		return (NodeViewElement) super.removeAttribute( attributeName );
	}

	@Override
	public NodeViewElement setName( String name ) {
		return (NodeViewElement) super.setName( name );
	}

	@Override
	public NodeViewElement setCustomTemplate( String customTemplate ) {
		return (NodeViewElement) super.setCustomTemplate( customTemplate );
	}

	@Override
	protected NodeViewElement setElementType( String elementType ) {
		return (NodeViewElement) super.setElementType( elementType );
	}

	@Override
	public NodeViewElement addChild( @NonNull ViewElement element ) {
		return (NodeViewElement) super.addChild( element );
	}

	@Override
	public NodeViewElement addChildren( @NonNull Collection<? extends ViewElement> elements ) {
		return (NodeViewElement) super.addChildren( elements );
	}

	@Override
	public NodeViewElement addFirstChild( @NonNull ViewElement element ) {
		return (NodeViewElement) super.addFirstChild( element );
	}

	@Override
	public NodeViewElement clearChildren() {
		return (NodeViewElement) super.clearChildren();
	}

	@Override
	public NodeViewElement apply( @NonNull Consumer<ContainerViewElement> consumer ) {
		return (NodeViewElement) super.apply( consumer );
	}

	@Override
	public NodeViewElement set( WitherSetter... setters ) {
		super.set( setters );
		return this;
	}

	@Override
	public NodeViewElement remove( WitherRemover... functions ) {
		super.remove( functions );
		return this;
	}

	@Override
	public <U extends ViewElement> NodeViewElement applyUnsafe( @NonNull Consumer<U> consumer ) {
		return (NodeViewElement) super.applyUnsafe( consumer );
	}
}
