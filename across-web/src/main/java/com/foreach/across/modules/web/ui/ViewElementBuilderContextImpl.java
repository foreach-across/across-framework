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

import com.foreach.across.core.support.AttributeSupport;
import com.foreach.across.core.support.ReadableAttributes;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Standard implementation of a {@link ViewElementBuilderContext} that optionally allows a parent set of attributes
 * to be provided.  Attributes from the parent will be visible in the context, but attributes set on the builder
 * context directly will hide the same attributes from the parent.  Hiding with a non-null value is possible, but
 * removing attributes entirely from the parent is not.</p>
 * <p>For example when creating a new context based on a {@link Model}, all attributes from the model will be
 * available on the context.  Additional attributes can be added to the context only and will not be available in the
 * original Model.  Changes to the model should propagate to the model (depending on the model implementation).</p>
 * <p>Custom implementations can override {@link #setParent(ReadableAttributes)} to allow the parent to be set
 * after construction.</p>
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.ui.elements.IteratorViewElementBuilderContext
 */
public class ViewElementBuilderContextImpl extends AttributeSupport implements ViewElementBuilderContext
{
	private static class AttributesMapWrapper extends AttributeSupport
	{
		public AttributesMapWrapper( Map<String, Object> backingMap ) {
			super( backingMap );
		}
	}

	private ReadableAttributes parent;

	public ViewElementBuilderContextImpl() {
	}

	public ViewElementBuilderContextImpl( Model parent ) {
		this( parent.asMap() );
	}

	public ViewElementBuilderContextImpl( Map<String, Object> parent ) {
		this( new AttributesMapWrapper( parent ) );
	}

	public ViewElementBuilderContextImpl( ReadableAttributes parent ) {
		setParent( parent );
	}

	protected void setParent( ReadableAttributes parent ) {
		this.parent = parent;
	}

	protected ReadableAttributes getParent() {
		return parent;
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
	public <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
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

		return merged;
	}

	@Override
	public String[] attributeNames() {
		Map<String, Object> map = attributeMap();
		String[] names = map.keySet().toArray( new String[map.size()] );
		Arrays.sort( names );

		return names;
	}
}
