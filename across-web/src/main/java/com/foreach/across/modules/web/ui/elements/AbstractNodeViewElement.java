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

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.support.CssClassAttributeUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for a {@link HtmlViewElement} that supports child nodes.
 *
 * @author Arne Vandamme
 * @see NodeViewElement
 * @see AbstractVoidNodeViewElement
 */
@Accessors(chain = true)
public abstract class AbstractNodeViewElement extends ContainerViewElement implements HtmlViewElement
{
	private Map<String, Object> attributes = new HashMap<>();

	@NonNull
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private String tagName;

	@Getter
	@Setter
	private String htmlId;

	protected AbstractNodeViewElement( String tagName ) {
		setElementType( StandardViewElements.NODE );
		setTagName( tagName );
	}

	@Override
	public AbstractNodeViewElement addCssClass( String... cssClass ) {
		CssClassAttributeUtils.addCssClass( attributes, cssClass );
		return this;
	}

	public boolean hasCssClass( String cssClass ) {
		return CssClassAttributeUtils.hasCssClass( attributes, cssClass );
	}

	public AbstractNodeViewElement removeCssClass( String... cssClass ) {
		CssClassAttributeUtils.removeCssClass( attributes, cssClass );
		return this;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public AbstractNodeViewElement setAttributes( @NonNull Map<String, Object> attributes ) {
		this.attributes = attributes;
		return this;
	}

	public AbstractNodeViewElement setAttribute( String attributeName, Object attributeValue ) {
		attributes.put( attributeName, attributeValue );
		return this;
	}

	public AbstractNodeViewElement addAttributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
		return this;
	}

	public AbstractNodeViewElement removeAttribute( String attributeName ) {
		attributes.remove( attributeName );
		return this;
	}

	@Override
	public Object getAttribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	@Override
	public <V, U extends V> U getAttribute( String attributeName, Class<V> expectedType ) {
		return returnIfType( attributes.get( attributeName ), expectedType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	@Override
	public AbstractNodeViewElement setName( String name ) {
		return (AbstractNodeViewElement) super.setName( name );
	}

	@Override
	public AbstractNodeViewElement setCustomTemplate( String customTemplate ) {
		return (AbstractNodeViewElement) super.setCustomTemplate( customTemplate );
	}

	@Override
	protected AbstractNodeViewElement setElementType( String elementType ) {
		return (AbstractNodeViewElement) super.setElementType( elementType );
	}

	@Override
	public AbstractNodeViewElement addChild( @NonNull ViewElement element ) {
		return (AbstractNodeViewElement) super.addChild( element );
	}

	@Override
	public AbstractNodeViewElement addChildren( @NonNull Collection<? extends ViewElement> elements ) {
		return (AbstractNodeViewElement) super.addChildren( elements );
	}

	@Override
	public AbstractNodeViewElement addFirstChild( @NonNull ViewElement element ) {
		return (AbstractNodeViewElement) super.addFirstChild( element );
	}

	@Override
	public AbstractNodeViewElement clearChildren() {
		return (AbstractNodeViewElement) super.clearChildren();
	}

	@Override
	public AbstractNodeViewElement apply( @NonNull Consumer<ContainerViewElement> consumer ) {
		return (AbstractNodeViewElement) super.apply( consumer );
	}

	@Override
	public <U extends ViewElement> AbstractNodeViewElement applyUnsafe( @NonNull Consumer<U> consumer ) {
		return (AbstractNodeViewElement) super.applyUnsafe( consumer );
	}

	@SuppressWarnings("unchecked")
	protected <V, U extends V> U returnIfType( Object value, Class<V> elementType ) {
		return elementType.isInstance( value ) ? (U) value : null;
	}

	@Override
	public AbstractNodeViewElement set( WitherSetter... setters ) {
		super.set( setters );
		return this;
	}

	@Override
	public AbstractNodeViewElement remove( WitherRemover... functions ) {
		super.remove( functions );
		return this;
	}

	@Override
	public <U> U get( WitherGetter<?, U> function ) {
		return super.get( function );
	}
}
