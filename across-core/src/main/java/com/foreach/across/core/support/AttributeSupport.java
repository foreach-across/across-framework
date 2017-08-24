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

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Support class for {@link WritableAttributes}, providing a base implementation of all methods.
 * To be extended by subclasses.  See {@link AttributeOverridingSupport} for a base implementation that
 * supports overriding attributes from a parent.
 *
 * @see AttributeOverridingSupport
 */
public abstract class AttributeSupport implements WritableAttributes
{
	private final Map<String, Object> attributes;

	public AttributeSupport() {
		this( new HashMap<String, Object>() );
	}

	/**
	 * @param backingMap map implementation that should be used as source
	 */
	protected AttributeSupport( Map<String, Object> backingMap ) {
		this.attributes = backingMap;
	}

	@Override
	public <Y> void setAttribute( Class<Y> attributeType, Y attributeValue ) {
		Assert.notNull( attributeType );

		setAttribute( attributeType.getName(), attributeValue );
	}

	@Override
	public void setAttributes( Map<String, Object> attributes ) {
		Assert.notNull( attributes );
		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			setAttribute( attribute.getKey(), attribute.getValue() );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> Y removeAttribute( Class<Y> attributeType ) {
		Assert.notNull( attributeType );
		return (Y) attributes.remove( attributeType.getName() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y, V extends Y> V getAttribute( Class<Y> attributeType ) {
		Assert.notNull( attributeType );
		return (V) getAttribute( attributeType.getName() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
		return (Y) getAttribute( attributeName );
	}

	@Override
	public boolean hasAttribute( Class<?> attributeType ) {
		return hasAttribute( attributeType.getName() );
	}

	@Override
	public void setAttribute( String name, Object value ) {
		Assert.notNull( name, "Name must not be null" );
		this.attributes.put( name, value );
	}

	@Override
	public Object getAttribute( String name ) {
		Assert.notNull( name, "Name must not be null" );
		return this.attributes.get( name );
	}

	@Override
	public Object removeAttribute( String name ) {
		Assert.notNull( name, "Name must not be null" );
		return this.attributes.remove( name );
	}

	@Override
	public boolean hasAttribute( String name ) {
		Assert.notNull( name, "Name must not be null" );
		return this.attributes.containsKey( name );
	}

	@Override
	public String[] attributeNames() {
		String[] names = this.attributes.keySet().toArray( new String[this.attributes.size()] );
		Arrays.sort( names );

		return names;
	}

	@Override
	public Map<String, Object> attributeMap() {
		return Collections.unmodifiableMap( attributes );
	}
}
