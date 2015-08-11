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

import java.util.Map;

/**
 * Adds base properties for a HTML node, like css class and html id support.
 *
 * @author Arne Vandamme
 */
public interface HtmlViewElement extends MutableViewElement
{
	String getTagName();

	void addCssClass( String... cssClass );

	boolean hasCssClass( String cssClass );

	void removeCssClass( String... cssClass );

	void setHtmlId( String id );

	String getHtmlId();

	Map<String, Object> getAttributes();

	void setAttributes( Map<String, Object> attributes );

	void setAttribute( String attributeName, Object attributeValue );

	void addAttributes( Map<String, Object> attributes );

	void removeAttribute( String attributeName );

	Object getAttribute( String attributeName );

	<V> V getAttribute( String attributeName, Class<V> expectedType );

	boolean hasAttribute( String attributeName );
}