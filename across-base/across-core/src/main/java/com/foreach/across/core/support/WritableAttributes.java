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
package com.foreach.across.core.support;

import org.springframework.core.AttributeAccessor;

import java.util.Map;

/**
 * Interface defining a generic contract for attaching and accessing metadata
 * to/from arbitrary objects.  Writable implementation of {@link ReadableAttributes}.
 */
public interface WritableAttributes extends ReadableAttributes, AttributeAccessor
{
	/**
	 * Set the attribute defined by {@code attributeType} to the supplied {@code value}.
	 * If {@code value} is {@code null}, the attribute is {@link #removeAttribute removed}.
	 *
	 * @param attributeType  the unique attribute key
	 * @param attributeValue the attribute value to be attached
	 */
	<Y> void setAttribute( Class<Y> attributeType, Y attributeValue );

	/**
	 * Copy all attributes from the source map into the current set.
	 *
	 * @param attributes to set
	 */
	void setAttributes( Map<String, Object> attributes );

	/**
	 * Remove the attribute identified by {@code attributeType} and return its value.
	 * Return {@code null} if no attribute under {@code attributeType} is found.
	 *
	 * @param attributeType the unique attribute key
	 * @return the last value of the attribute, if any
	 */
	<Y> Y removeAttribute( Class<Y> attributeType );
}
