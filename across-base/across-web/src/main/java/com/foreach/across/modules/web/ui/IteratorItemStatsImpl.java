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

/**
 * Represents some contextual information about an item that is part of an iterable like collection.
 * Keeps track of the item itself, its index and next/previous information.
 *
 * @author Arne Vandamme
 */
public class IteratorItemStatsImpl<ITEM> implements IteratorItemStats<ITEM>
{
	private boolean first, last;
	private int index;
	private ITEM item;

	public IteratorItemStatsImpl( ITEM item, int index, boolean last ) {
		this( item, index, index == 0, last );
	}

	public IteratorItemStatsImpl( ITEM item, int index, boolean first, boolean last ) {
		this.first = first;
		this.last = last;
		this.index = index;
		this.item = item;
	}

	/**
	 * @return Index of the item in the collection.
	 */
	@Override
	public int getIndex() {
		return index;
	}

	/**
	 * @return True if there are previous items in the collection.
	 */
	@Override
	public boolean hasPrevious() {
		return !isFirst();
	}

	/**
	 * @return True if this item is the first one in the collection.
	 */
	@Override
	public boolean isFirst() {
		return first;
	}

	/**
	 * @return True if there are more items in the collection after this one.
	 */
	@Override
	public boolean hasNext() {
		return !isLast();
	}

	/**
	 * @return True if this item is the last item in the collection.
	 */
	@Override
	public boolean isLast() {
		return last;
	}

	/**
	 * @return item itself
	 */
	@Override
	public ITEM getItem() {
		return item;
	}
}
