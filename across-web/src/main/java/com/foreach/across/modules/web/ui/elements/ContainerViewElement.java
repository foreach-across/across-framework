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
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

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
	 * @return modifiable list of child elements this container has
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
	public void addChild( ViewElement element ) {
		Assert.notNull( element );
		children.add( element );
	}

	/**
	 * Adds a child as the first one to this container.
	 *
	 * @param element to add
	 */
	public void addFirstChild( ViewElement element ) {
		Assert.notNull( element );
		children.add( 0, element );
	}

	/**
	 * Remove a child from this container.
	 *
	 * @param element to remove
	 * @return true if child was present and has been removed
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
}
