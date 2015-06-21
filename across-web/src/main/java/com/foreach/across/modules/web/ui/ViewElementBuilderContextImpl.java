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

import com.foreach.across.modules.web.ui.support.AttributeSupport;
import com.foreach.across.modules.web.ui.support.AttributesMapWrapper;
import com.foreach.across.modules.web.ui.support.ReadableAttributes;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard implementation of a {@link ViewElementBuilderContext} that allows a parent set of {@link ReadableAttributes}
 * to be provided.  Attributes set on the builder context directly will hide the same attributes in the parent.
 *
 * @author Arne Vandamme
 */
public class ViewElementBuilderContextImpl extends AttributeSupport implements ViewElementBuilderContext
{
	private ReadableAttributes parent;

	public ViewElementBuilderContextImpl() {
	}

	public ViewElementBuilderContextImpl( Model parent ) {
		this( parent.asMap() );
	}

	public ViewElementBuilderContextImpl( Map parent ) {
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
	public <Y> Y getAttribute( String attributeName ) {
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
	public Map<Object, Object> getAttributes() {
		Map<Object, Object> merged = new HashMap<>();
		if ( parent != null ) {
			merged.putAll( parent.getAttributes() );
		}
		merged.putAll( super.getAttributes() );

		return merged;
	}
}
