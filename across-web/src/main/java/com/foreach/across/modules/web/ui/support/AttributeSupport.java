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
package com.foreach.across.modules.web.ui.support;

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AttributeSupport implements ReadableAttributes, WritableAttributes
{
	private final Map<Object, Object> attributes;

	public AttributeSupport() {
		this( new HashMap<>() );
	}

	protected AttributeSupport( Map<Object, Object> backingMap ) {
		this.attributes = backingMap;
	}

	public <Y> void addAttribute( Class<Y> attributeType, Y attributeValue ) {
		Assert.notNull( attributeType );

		attributes.put( attributeType, attributeValue );
	}

	public void addAttribute( String attributeName, Object attributeValue ) {
		Assert.notNull( attributeName );

		attributes.put( attributeName, attributeValue );
	}

	public void addAllAttributes( Map<Object, Object> attributes ) {
		for ( Map.Entry<Object, Object> attribute : attributes.entrySet() ) {
			Assert.notNull( attribute.getKey() );

			if ( attribute.getKey() instanceof String || attribute.getKey() instanceof Class ) {
				this.attributes.put( attribute.getKey(), attribute.getValue() );
			}
			else {
				throw new RuntimeException(
						"Only attributes with a non-null key of type String or Class can be added" );
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <Y> Y removeAttribute( Class<Y> attributeType ) {
		return (Y) attributes.remove( attributeType );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y removeAttribute( String attributeName ) {
		return (Y) attributes.remove( attributeName );
	}

	@SuppressWarnings("unchecked")
	public <Y, V extends Y> V getAttribute( Class<Y> attributeType ) {
		return (V) attributes.get( attributeType );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y getAttribute( String attributeName ) {
		return (Y) attributes.get( attributeName );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
		return (Y) attributes.get( attributeName );
	}

	public boolean hasAttribute( Class<?> attributeType ) {
		return attributes.containsKey( attributeType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	public Map<Object, Object> getAttributes() {
		return Collections.unmodifiableMap( attributes );
	}
}
