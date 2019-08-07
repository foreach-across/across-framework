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

import java.util.*;

/**
 * <p>Extension to {@link AttributeSupport} that optionally takes a parent {@link ReadableAttributes} instance.
 * An attribute is present if it is set in the child instance or in the parent, where attribute values from
 * the parent will only be used if not set in the child.</p>
 * <p>This implementation is less performant than the regular {@link AttributeSupport} because of dual lookups
 * and merge semantics.</p>
 *
 * @author Arne Vandamme
 */
public class AttributeOverridingSupport extends AttributeSupport
{
	private ReadableAttributes parent;

	protected AttributeOverridingSupport() {
	}

	protected AttributeOverridingSupport( ReadableAttributes parent ) {
		setParent( parent );
	}

	protected ReadableAttributes getParent() {
		return parent;
	}

	protected void setParent( ReadableAttributes parent ) {
		this.parent = parent;
	}

	@Override
	public <Y, V extends Y> V getAttribute( Class<Y> attributeType ) {
		if ( parent != null ) {
			if ( super.hasAttribute( attributeType ) ) {
				return super.getAttribute( attributeType );
			}
			else {
				return parent.getAttribute( attributeType );
			}
		}

		return super.getAttribute( attributeType );
	}

	@Override
	public Object getAttribute( String attributeName ) {
		if ( parent != null ) {
			if ( super.hasAttribute( attributeName ) ) {
				return super.getAttribute( attributeName );
			}
			else {
				return parent.getAttribute( attributeName );
			}
		}

		return super.getAttribute( attributeName );
	}

	@Override
	public <Y, V extends Y> V getAttribute( String attributeName, Class<Y> attributeType ) {
		if ( parent != null ) {
			if ( super.hasAttribute( attributeName ) ) {
				return super.getAttribute( attributeName, attributeType );
			}
			else {
				return parent.getAttribute( attributeName, attributeType );
			}
		}

		return super.getAttribute( attributeName, attributeType );
	}

	@Override
	public boolean hasAttribute( Class<?> attributeType ) {
		return super.hasAttribute( attributeType )
				|| ( parent != null && parent.hasAttribute( attributeType ) );
	}

	@Override
	public boolean hasAttribute( String attributeName ) {
		return super.hasAttribute( attributeName )
				|| ( parent != null && parent.hasAttribute( attributeName ) );
	}

	@Override
	public Map<String, Object> attributeMap() {
		Map<String, Object> merged = new HashMap<>();
		if ( parent != null ) {
			merged.putAll( parent.attributeMap() );
		}
		merged.putAll( super.attributeMap() );

		return Collections.unmodifiableMap( merged );
	}

	@Override
	public String[] attributeNames() {
		Map<String, Object> map = attributeMap();
		String[] names = map.keySet().toArray( new String[0] );
		Arrays.sort( names );

		return names;
	}

	public InheritedAttributeValue<Object> findAttribute( String attributeName ) {
		return findAttribute( attributeName, Object.class );
	}

	public <U> InheritedAttributeValue<U> findAttribute( Class<U> attributeType ) {
		return findAttribute( attributeType.getName(), attributeType );
	}

	public <U> InheritedAttributeValue<U> findAttribute( String attributeName, Class<U> attributeType ) {
		return findAttribute( attributeName, attributeType, 0 );
	}

	private <U> InheritedAttributeValue<U> findAttribute( String attributeName, Class<U> attributeType, int ancestorLevel ) {
		if ( super.hasAttribute( attributeName ) ) {
			return new InheritedAttributeValue<>( Optional.ofNullable( super.getAttribute( attributeName, attributeType ) ), attributeName, ancestorLevel );
		}
		else if ( parent != null ) {
			if ( parent instanceof AttributeOverridingSupport ) {
				return ( (AttributeOverridingSupport) parent ).findAttribute( attributeName, attributeType, ancestorLevel + 1 );
			}
			else if ( parent.hasAttribute( attributeName ) ) {
				return new InheritedAttributeValue<>( Optional.ofNullable( parent.getAttribute( attributeName, attributeType ) ), attributeName,
				                                      ancestorLevel + 1 );
			}
		}

		return new InheritedAttributeValue<>( Optional.empty(), attributeName, -1 );
	}
}
