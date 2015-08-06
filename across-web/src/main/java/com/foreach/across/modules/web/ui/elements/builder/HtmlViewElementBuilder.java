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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.elements.HtmlViewElement;

import java.util.Map;

/**
 * @author Arne Vandamme
 */
public interface HtmlViewElementBuilder<T extends HtmlViewElement, SELF extends HtmlViewElementBuilder<T, SELF>>
{
	SELF htmlId( String htmlId );

	SELF css( String... cssClasses );

	SELF removeCss( String... cssClasses );

	SELF attribute( String name, Object value );

	SELF attributes( Map<String, Object> attributes );

	SELF removeAttribute( String name );

	SELF clearAttributes();
}
