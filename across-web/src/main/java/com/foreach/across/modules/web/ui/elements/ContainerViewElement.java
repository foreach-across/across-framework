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
import com.foreach.across.modules.web.ui.ViewElements;

/**
 * Simplest implementation of {@link com.foreach.across.modules.web.ui.ViewElements} that also implements
 * {@link com.foreach.across.modules.web.ui.ViewElement}.  A container is a named
 * collection of elements that allows configuration of a custom template for rendering.
 * <p/>
 * Unless a custom template is being used, a collection does not add additional output but simply renders
 * its children in order.
 *
 * @author Arne Vandamme
 */
public class ContainerViewElement extends ViewElements implements MutableViewElement
{
	public static final String ELEMENT_TYPE = StandardViewElements.CONTAINER;

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
}
