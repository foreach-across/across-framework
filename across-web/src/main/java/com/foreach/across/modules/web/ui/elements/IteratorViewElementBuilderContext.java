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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.IteratorItemStats;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.support.AttributeSupport;
import com.foreach.across.modules.web.ui.support.WritableAttributes;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapping {@link ViewElementBuilderContext} that is used by a {@link ViewElementGenerator} and provides
 * access to the item being generated and the possible iteration context.
 * <p/>
 * This context optionally takes a parent context.  All attributes from the parent context will be inherited
 * and can be masked or replaced in the iterator context.  Note that it is impossible to remove attributes from the
 * parent context, only to replace them by null values.
 *
 * @author Arne Vandamme
 */
public class IteratorViewElementBuilderContext<ITEM> extends AttributeSupport
		implements ViewElementBuilderContext, WritableAttributes, IteratorItemStats<ITEM>
{
	private ViewElementBuilderContext parentContext;
	private IteratorItemStats<ITEM> itemStats;

	public IteratorViewElementBuilderContext( IteratorItemStats<ITEM> itemStats ) {
		Assert.notNull( itemStats );
		this.itemStats = itemStats;
	}

	/**
	 * @return True if previous items have been generated.
	 */
	@Override
	public boolean hasPrevious() {
		return itemStats.hasPrevious();
	}

	/**
	 * @return True if this item is the first one being generated.
	 */
	@Override
	public boolean isFirst() {
		return itemStats.isFirst();
	}

	/**
	 * @return True if another item needs generating.
	 */
	@Override
	public boolean hasNext() {
		return itemStats.hasNext();
	}

	/**
	 * @return True if this item is the last one being generated.
	 */
	@Override
	public boolean isLast() {
		return itemStats.isLast();
	}

	/**
	 * @return the curren iteration item
	 */
	@Override
	public ITEM getItem() {
		return itemStats.getItem();
	}

	/**
	 * @return Index of the item in the collection.
	 */
	@Override
	public int getIndex() {
		return itemStats.getIndex();
	}

	/**
	 * @return parent context that attributes are inherited from
	 */
	public ViewElementBuilderContext getParentContext() {
		return parentContext;
	}

	/**
	 * Set the parent context that this iteration context wraps around.  All attributes from the parent context
	 * will be available in the iteration scope.
	 *
	 * @param parentContext to inherit the attributes from
	 */
	public void setParentContext( ViewElementBuilderContext parentContext ) {
		this.parentContext = parentContext;
	}

	@Override
	public <Y, V extends Y> V getAttribute( Class<Y> attributeType ) {
		if ( parentContext != null ) {
			if ( super.hasAttribute( attributeType ) ) {
				return super.getAttribute( attributeType );
			}
			else {
				return parentContext.getAttribute( attributeType );
			}
		}

		return super.getAttribute( attributeType );
	}

	@Override
	public <Y> Y getAttribute( String attributeName ) {
		if ( parentContext != null ) {
			if ( super.hasAttribute( attributeName ) ) {
				return super.getAttribute( attributeName );
			}
			else {
				return parentContext.getAttribute( attributeName );
			}
		}

		return super.getAttribute( attributeName );
	}

	@Override
	public <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
		if ( parentContext != null ) {
			if ( super.hasAttribute( attributeName ) ) {
				return super.getAttribute( attributeName, attributeType );
			}
			else {
				return parentContext.getAttribute( attributeName, attributeType );
			}
		}

		return super.getAttribute( attributeName, attributeType );
	}

	@Override
	public boolean hasAttribute( Class<?> attributeType ) {
		return super.hasAttribute( attributeType )
				|| ( parentContext != null && parentContext.hasAttribute( attributeType ) );
	}

	@Override
	public boolean hasAttribute( String attributeName ) {
		return super.hasAttribute( attributeName )
				|| ( parentContext != null && parentContext.hasAttribute( attributeName ) );
	}

	@Override
	public Map<Object, Object> getAttributes() {
		Map<Object, Object> merged = new HashMap<>();
		if ( parentContext != null ) {
			merged.putAll( parentContext.getAttributes() );
		}
		merged.putAll( super.getAttributes() );

		return merged;
	}
}
