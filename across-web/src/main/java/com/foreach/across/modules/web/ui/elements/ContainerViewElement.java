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

import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A container is a named collection of elements ({@link #getChildren()} that allows
 * configuration of a custom template for rendering.  This is a base class for every other {@link ViewElement}
 * that supports children.
 * <p>
 * Unless a custom template is being used, a collection does not add additional output but simply renders
 * its children in order.
 * </p>
 * <p>
 * Complex operations on containers (including on children that are in turn containers) can easily be done
 * using the {@link com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils}.
 * </p>
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils
 */
public class ContainerViewElement implements MutableViewElement
{
	public static final String ELEMENT_TYPE = StandardViewElements.CONTAINER;

	private final List<ViewElement> children = new ArrayList<>();

	private String name, customTemplate, elementType;

	public ContainerViewElement() {
		setElementType( ELEMENT_TYPE );
	}

	public ContainerViewElement( String name ) {
		setName( name );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public String getCustomTemplate() {
		return customTemplate;
	}

	@Override
	public void setCustomTemplate( String customTemplate ) {
		this.customTemplate = customTemplate;
	}

	@Override
	public String getElementType() {
		return elementType;
	}

	protected void setElementType( String elementType ) {
		this.elementType = elementType;
	}

	/**
	 * Get the stream of all {@link ViewElement} instances this container represents (excluding itself).
	 * All these elements can be found using any of the {@link #find(String)} methods.
	 * <p/>
	 * By default this is the same collection as the direct children, but that is not a requirement.
	 * <p/>
	 * WARNING: returning a different collection then the direct children will impact modification methods.
	 * Elements can be found but not replaced or removed if they are not a part of the children collection.
	 *
	 * @return stream
	 */
	public Stream<ViewElement> elementStream() {
		return getChildren().stream();
	}

	/**
	 * Get the list of direct children this container represents.
	 *
	 * @return list of child elements this container represents
	 */
	@SuppressWarnings("all")
	public List<ViewElement> getChildren() {
		return children;
	}

	/**
	 * Add a child to this container.
	 *
	 * @param element to add
	 */
	public void addChild( @NonNull ViewElement element ) {
		children.add( element );
	}

	/**
	 * Add a collection of children to this container.
	 *
	 * @param elements to add
	 */
	public void addChildren( @NonNull Collection<ViewElement> elements ) {
		children.addAll( elements );
	}

	/**
	 * Adds a child as the first one to this container.
	 *
	 * @param element to add
	 */
	public void addFirstChild( @NonNull ViewElement element ) {
		children.add( 0, element );
	}

	/**
	 * Remove a direct child from this container.
	 *
	 * @param element to remove
	 * @return true if child was present and has been removed
	 * @see #removeFromTree(String)
	 */
	public boolean removeChild( ViewElement element ) {
		return children.remove( element );
	}

	/**
	 * @return true if this container has child elements
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * Remove all children from this container.
	 */
	public void clearChildren() {
		children.clear();
	}

	/**
	 * Apply a consumer to this container. Useful for providing extensions that wish
	 * to provide actions on the container itself.
	 *
	 * @param consumer to execute
	 */
	public void apply( @NonNull Consumer<ContainerViewElement> consumer ) {
		consumer.accept( this );
	}

	/**
	 * Apply a consumer to this container. Useful for providing extensions that wish
	 * to provide actions on the container itself.
	 * <p/>
	 * This method allows you to pass any {@link Consumer} for a generic {@link ViewElement}.
	 * Note that if you pass a consumer for a type that is not compatible with
	 * {@link ContainerViewElement}, you will receive class cast exceptions at runtime, hence the <em>unsafe</em>.
	 *
	 * @param consumer to execute
	 */
	@SuppressWarnings("unchecked")
	public <U extends ViewElement> void applyUnsafe( @NonNull Consumer<U> consumer ) {
		consumer.accept( (U) this );
	}

	/**
	 * Find the first child element with the given name in the container.  Will recursive through all children
	 * that are also {@link ContainerViewElement} implementations.  Search will be top-down and the first
	 * matching element will be returned.
	 *
	 * @return element if found
	 * @see #findOrSelf(String)
	 */
	public Optional<ViewElement> find( String elementName ) {
		return ContainerViewElementUtils.find( this, elementName );
	}

	/**
	 * Find the first element with the given name in the container.
	 * Will first check the container itself and return it if it matches.  If not, will check all children
	 * and will recursive through all children that are also {@link ContainerViewElement} implementations.
	 * Search will be top-down and the first matching element will be returned.
	 *
	 * @return element if found
	 */
	public Optional<ViewElement> findOrSelf( String name ) {
		return ContainerViewElementUtils.findOrSelf( this, name );
	}

	/**
	 * Find the first child element with the given name that is also of the required type.
	 * Will recursive through all children that are also {@link ContainerViewElement} implementations.
	 * Search will be top-down and the first matching element will be returned.  If no element has that
	 * name or it is not of the required type, {@link Optional#empty()} will be returned.
	 *
	 * @return element if found
	 * @see #findOrSelf(String)
	 */
	public <V extends ViewElement> Optional<V> find( String elementName, Class<V> requiredType ) {
		return ContainerViewElementUtils.find( this, elementName, requiredType );
	}

	/**
	 * Find the first element with the given name that is also of the required type.
	 * Will first check the container itself and return it if it matches.  If not, will check all children
	 * and will recursive through all children that are also {@link ContainerViewElement} implementations.
	 * Search will be top-down and the first matching element will be returned.
	 *
	 * @return element if found
	 */
	public <V extends ViewElement> Optional<V> findOrSelf( String elementName, Class<V> requiredType ) {
		return ContainerViewElementUtils.findOrSelf( this, name, requiredType );
	}

	/**
	 * Creates a flattened stream of all elements in the container.
	 * Will recurse top-down through all children that are also {@link ContainerViewElement} implementations.
	 *
	 * @return flattened stream
	 */
	public Stream<ViewElement> flatStream() {
		return ContainerViewElementUtils.flatStream( this );
	}

	/**
	 * Find all elements in the container that are of the required type.
	 * Will recurse top-down through all children that are also {@link ContainerViewElement} implementations.
	 *
	 * @param requiredType the elements should have
	 * @param <V>          type
	 * @return stream of matching elements
	 */
	public <V extends ViewElement> Stream<V> findAll( Class<V> requiredType ) {
		return ContainerViewElementUtils.findAll( this, requiredType );
	}

	/**
	 * Find all elements in the container that are of the required type and match the additional predicate.
	 * Will recurse top-down through all children that are also {@link ContainerViewElement} implementations.
	 *
	 * @param requiredType the elements should have
	 * @param predicate    additional predicate the elements should match
	 * @param <V>          type
	 * @return stream of matching elements
	 */
	public <V extends ViewElement> Stream<V> findAll( Class<V> requiredType, Predicate<V> predicate ) {
		return ContainerViewElementUtils.findAll( this, requiredType, predicate );
	}

	/**
	 * Find all elements in the container that are of the required type and match the additional predicate.
	 * Will recurse top-down through all children that are also {@link ContainerViewElement} implementations.
	 *
	 * @param predicate additional predicate the elements should match
	 * @return stream of matching elements
	 */
	public Stream<ViewElement> findAll( Predicate<ViewElement> predicate ) {
		return ContainerViewElementUtils.findAll( this, predicate );
	}

	/**
	 * Removes all elements with the given names from the tree.
	 *
	 * @param elementNames names of the elements to remove
	 * @return element that has been remove
	 */
	public Stream<ViewElement> removeAllFromTree( String... elementNames ) {
		return ContainerViewElementUtils.removeAll( this, elementNames );
	}

	/**
	 * Remove the first element with the given name from the container or any of its children.
	 * The container will be searched top-down recursively until an element is found. The return value will be empty
	 * if no element with the given name was present.
	 *
	 * @param elementName name of the element to remove
	 * @return element that has been remove
	 */
	public Optional<ViewElement> removeFromTree( String elementName ) {
		return ContainerViewElementUtils.remove( this, elementName );
	}

	/**
	 * Remove the given element from the container.  Will search the container top-down recursively.
	 *
	 * @param element to remove
	 * @return true if element was present and has been removed
	 */
	public boolean removeFromTree( ViewElement element ) {
		return ContainerViewElementUtils.remove( this, element );
	}
}
