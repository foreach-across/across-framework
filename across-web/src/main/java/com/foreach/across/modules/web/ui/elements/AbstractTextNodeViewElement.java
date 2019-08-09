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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class that extends {@link AbstractNodeViewElement} with the {@link ConfigurableTextViewElement}.
 * If {@link #setText(String)} is set, a {@link TextViewElement} will be added.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Accessors(chain = true)
public abstract class AbstractTextNodeViewElement extends AbstractNodeViewElement implements ConfigurableTextViewElement
{
	/**
	 * Configure a simple text body for the alert.  Additional children will be after.
	 */
	@Getter
	@Setter
	private String text;

	protected AbstractTextNodeViewElement( String tagName ) {
		super( tagName );
	}

	@Override
	protected AbstractTextNodeViewElement setTagName( @NonNull String tagName ) {
		return (AbstractTextNodeViewElement) super.setTagName( tagName );
	}

	@Override
	public AbstractTextNodeViewElement setHtmlId( String htmlId ) {
		return (AbstractTextNodeViewElement) super.setHtmlId( htmlId );
	}

	@Override
	public AbstractTextNodeViewElement addCssClass( String... cssClass ) {
		return (AbstractTextNodeViewElement) super.addCssClass( cssClass );
	}

	@Override
	public AbstractTextNodeViewElement removeCssClass( String... cssClass ) {
		return (AbstractTextNodeViewElement) super.removeCssClass( cssClass );
	}

	@Override
	public AbstractTextNodeViewElement setAttributes( @NonNull Map<String, Object> attributes ) {
		return (AbstractTextNodeViewElement) super.setAttributes( attributes );
	}

	@Override
	public AbstractTextNodeViewElement setAttribute( String attributeName, Object attributeValue ) {
		return (AbstractTextNodeViewElement) super.setAttribute( attributeName, attributeValue );
	}

	@Override
	public AbstractTextNodeViewElement addAttributes( Map<String, Object> attributes ) {
		return (AbstractTextNodeViewElement) super.addAttributes( attributes );
	}

	@Override
	public AbstractTextNodeViewElement removeAttribute( String attributeName ) {
		return (AbstractTextNodeViewElement) super.removeAttribute( attributeName );
	}

	@Override
	public AbstractTextNodeViewElement setName( String name ) {
		return (AbstractTextNodeViewElement) super.setName( name );
	}

	@Override
	public AbstractTextNodeViewElement setCustomTemplate( String customTemplate ) {
		return (AbstractTextNodeViewElement) super.setCustomTemplate( customTemplate );
	}

	@Override
	protected AbstractTextNodeViewElement setElementType( String elementType ) {
		return (AbstractTextNodeViewElement) super.setElementType( elementType );
	}

	@Override
	public AbstractTextNodeViewElement addChild( @NonNull ViewElement element ) {
		return (AbstractTextNodeViewElement) super.addChild( element );
	}

	@Override
	public AbstractTextNodeViewElement addChildren( @NonNull Collection<? extends ViewElement> elements ) {
		return (AbstractTextNodeViewElement) super.addChildren( elements );
	}

	@Override
	public AbstractTextNodeViewElement addFirstChild( @NonNull ViewElement element ) {
		return (AbstractTextNodeViewElement) super.addFirstChild( element );
	}

	@Override
	public AbstractTextNodeViewElement clearChildren() {
		return (AbstractTextNodeViewElement) super.clearChildren();
	}

	@Override
	public AbstractTextNodeViewElement apply( @NonNull Consumer<ContainerViewElement> consumer ) {
		return (AbstractTextNodeViewElement) super.apply( consumer );
	}

	@Override
	public <U extends ViewElement> AbstractTextNodeViewElement applyUnsafe( @NonNull Consumer<U> consumer ) {
		return (AbstractTextNodeViewElement) super.applyUnsafe( consumer );
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || text != null;
	}

	@Override
	public List<ViewElement> getChildren() {
		if ( text != null ) {
			List<ViewElement> children = new ArrayList<>();
			children.add( new TextViewElement( text ) );
			children.addAll( super.getChildren() );
			return children;
		}

		return super.getChildren();
	}

	@Override
	public AbstractTextNodeViewElement set( WitherSetter... setters ) {
		super.set( setters );
		return this;
	}

	@Override
	public AbstractTextNodeViewElement remove( WitherRemover... functions ) {
		super.remove( functions );
		return this;
	}
}
