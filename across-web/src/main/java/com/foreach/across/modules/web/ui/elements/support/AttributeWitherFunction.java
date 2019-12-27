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
package com.foreach.across.modules.web.ui.elements.support;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Function for {@link com.foreach.across.modules.web.ui.ViewElement.Wither} which allows for
 * setting and removing of attributes on a {@link com.foreach.across.modules.web.ui.elements.HtmlViewElement}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class AttributeWitherFunction<T> implements ViewElement.WitherRemover<HtmlViewElement>, ViewElement.WitherGetter<HtmlViewElement, T>, Predicate<HtmlViewElement>
{
	@NonNull
	private final String attributeKey;

	@Override
	public void removeFrom( HtmlViewElement target ) {
		target.removeAttribute( attributeKey );
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getValueFrom( HtmlViewElement target ) {
		return (T) target.getAttribute( attributeKey );
	}

	@Override
	public boolean test( HtmlViewElement htmlViewElement ) {
		return htmlViewElement.hasAttribute( attributeKey );
	}

	public AttributeValueWitherFunction<T> withValue( T value ) {
		return new AttributeValueWitherFunction<>( attributeKey, value );
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class AttributeValueWitherFunction<T> implements ViewElement.WitherSetter<HtmlViewElement>, Predicate<HtmlViewElement>
	{
		private final String attributeKey;
		private final T value;

		@Override
		public void applyTo( HtmlViewElement target ) {
			target.setAttribute( attributeKey, value );
		}

		@Override
		public boolean test( HtmlViewElement htmlViewElement ) {
			return Objects.equals( htmlViewElement.getAttribute( attributeKey ), value );
		}
	}
}
