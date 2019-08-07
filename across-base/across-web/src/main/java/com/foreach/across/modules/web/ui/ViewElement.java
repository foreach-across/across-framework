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
package com.foreach.across.modules.web.ui;

/**
 * Represents a {@link ViewElement} in its most simple form.  In a web context this is almost certainly
 * a HTML node or collection thereof.
 */
public interface ViewElement
{
	/**
	 * A ViewElement can have an internal name that identifies it within a
	 * {@link com.foreach.across.modules.web.ui.elements.ContainerViewElement}.  A name is optional but when given,
	 * is preferably unique within its container as most operations work on the first element with a specific name.
	 *
	 * @return Internal name of this element, can be null.
	 * @see com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils
	 */
	String getName();

	/**
	 * @return Type id of this view element.
	 */
	String getElementType();

	/**
	 * @return Custom template to use when rendering this view element.
	 */
	String getCustomTemplate();
}
