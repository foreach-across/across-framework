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
import lombok.NonNull;

import java.util.Map;

/**
 * @author Arne Vandamme
 */
public interface HtmlViewElementBuilder<T extends HtmlViewElement, SELF extends HtmlViewElementBuilder<T, SELF>>
{
	SELF htmlId( String htmlId );

	SELF css( String... cssClasses );

	SELF removeCss( String... cssClasses );

	/**
	 * Short-hand for adding a {@code data-} HTML attribute, but without having
	 * to specify the {@code data-} prefix yourself.
	 *
	 * @param name  attribute name (will be prefixed with {@code data-}
	 * @param value attribute value
	 * @return current builder
	 */
	default SELF data( @NonNull String name, Object value ) {
		return attribute( "data-" + name, value );
	}

	/**
	 * Short-hand for removing a {@code data-} HTML attribute.
	 *
	 * @param name attribute name (will be prefixed with {@code data-}
	 * @return current builder
	 */
	default SELF removeData( @NonNull String name ) {
		return removeAttribute( "data-" + name );
	}

	SELF attribute( String name, Object value );

	SELF attributes( Map<String, Object> attributes );

	SELF removeAttribute( String name );

	SELF clearAttributes();
}
