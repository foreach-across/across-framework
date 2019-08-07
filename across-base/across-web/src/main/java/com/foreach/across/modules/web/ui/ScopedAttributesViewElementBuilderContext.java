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

import com.foreach.across.core.support.InheritedAttributeValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of temporary modifications to an existing {@link ViewElementBuilderContext}.
 * Unlike creating a new builder context, this will apply changes to the existing context, but will
 * rollback those changes then {@link #close()} is called.
 *
 * @author Arne Vandamme
 * @since 3.1.1
 */
@RequiredArgsConstructor
public class ScopedAttributesViewElementBuilderContext implements AutoCloseable
{
	@NonNull
	private final ViewElementBuilderContext builderContext;

	private final Map<String, InheritedAttributeValue> originalValues = new HashMap<>();

	/**
	 * Override an attribute value. Passing {@code null} as attribute value is the same as calling {@link #removeAttribute(String)}.
	 *
	 * @param attributeType  type of the attribute to override
	 * @param attributeValue value to override the attribute with
	 * @return current builder context scope
	 */
	public <Y> ScopedAttributesViewElementBuilderContext withAttributeOverride( @NonNull Class<Y> attributeType, Y attributeValue ) {
		return withAttributeOverride( attributeType.getName(), attributeValue );
	}

	/**
	 * Override an attribute value. Passing {@code null} as attribute value is the same as calling {@link #removeAttribute(String)}.
	 *
	 * @param attributeName  name of the attribute to override
	 * @param attributeValue value to override the attribute with
	 * @return current builder context scope
	 */
	public ScopedAttributesViewElementBuilderContext withAttributeOverride( @NonNull String attributeName, Object attributeValue ) {
		originalValues.computeIfAbsent( attributeName, a -> builderContext.findAttribute( attributeName ) );
		builderContext.setAttribute( attributeName, attributeValue );
		return this;
	}

	/**
	 * Remove an attribute from the builder context.
	 *
	 * @param attributeType type of the attribute to remove
	 * @return current builder context scope
	 */
	public ScopedAttributesViewElementBuilderContext removeAttribute( @NonNull Class attributeType ) {
		return removeAttribute( attributeType.getName() );
	}

	/**
	 * Remove an attribute from the builder context.
	 *
	 * @param attributeName name of the attribute to remove
	 * @return current builder context scope
	 */
	public ScopedAttributesViewElementBuilderContext removeAttribute( @NonNull String attributeName ) {
		originalValues.computeIfAbsent( attributeName, a -> builderContext.findAttribute( attributeName ) );
		builderContext.removeAttribute( attributeName );
		return this;
	}

	@Override
	public void close() {
		originalValues.values()
		              .forEach( ov -> {
			              if ( !ov.exists() || !ov.isLocalAttribute() ) {
				              builderContext.removeAttribute( ov.getAttributeName() );
			              }
			              else {
				              builderContext.setAttribute( ov.getAttributeName(), ov.getValue() );
			              }
		              } );
	}
}
