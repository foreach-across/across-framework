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

import java.util.Map;

/**
 * Interface defining a generic contract for accessing metadata from arbitrary objects.
 * Attributes are defined by a {@code String} key.  This interface also provides shortcut
 * methods where an attribute is defined by a {@code Class} which name is the unique
 * attribute key and the value is assumed to be an instance of that {@code Class}.
 */
public interface ReadableAttributes
{
	/**
	 * Get the value of the attribute identified by {@code attributeType}.
	 * The attribute name will be the class name, and the value will be coerced
	 * to the type specified. Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeType the unique attribute type
	 * @return the current value of the attribute, if any
	 */
	<Y, V extends Y> V getAttribute( Class<Y> attributeType );

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeName the unique attribute key
	 * @return the current value of the attribute, if any
	 */
	Object getAttribute( String attributeName );

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * The value will be coerced to the {@code attributeType} specified.
	 * Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeName the unique attribute key
	 * @return the current value of the attribute, if any
	 */
	<Y> Y getAttribute( String attributeName, Class<Y> attributeType );

	/**
	 * Return {@code true} if the attribute identified by {@code attributeType} exists.
	 * Otherwise return {@code false}.
	 *
	 * @param attributeType the unique attribute key
	 */
	boolean hasAttribute( Class<?> attributeType );

	/**
	 * Return {@code true} if the attribute identified by {@code attributeName} exists.
	 * Otherwise return {@code false}.
	 *
	 * @param attributeName the unique attribute key
	 */
	boolean hasAttribute( String attributeName );

	/**
	 * Return the names of all attributes.
	 */
	String[] attributeNames();

	/**
	 * Return a {@link Map} view of all attributes.
	 *
	 * @return map containing all attributes
	 */
	Map<String, Object> attributeMap();
}
