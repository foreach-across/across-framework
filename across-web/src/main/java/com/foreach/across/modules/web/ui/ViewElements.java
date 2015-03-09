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

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Represents a manageable collection of {@link com.foreach.across.modules.web.ui.ViewElement}
 * instances.  Every element in the collection must have a unique name, and every
 * {@link com.foreach.across.modules.web.ui.ViewElements} will behave as part of the larger
 * collection.  That means that names must be unique across all elements in this collection directly and in any of
 * sub collections added through groups.
 *
 * @author Arne Vandamme
 */
public class ViewElements implements Iterable<ViewElement>
{
	private final LinkedList<ViewElement> elements = new LinkedList<>();

	/**
	 * Add the element directly to this collection.  If anywhere in this collection there
	 * is already an element with the same name, an exception will be thrown.
	 *
	 * @param element element to add
	 */
	public void add( ViewElement element ) {
		Assert.notNull( element );

		if ( contains( element.getName() ) ) {
			throw new IllegalArgumentException( "Set already contains a ViewElement with name " + element.getName() );
		}

		elements.add( element );
	}

	/**
	 * Add the element directly as the first element to this collection.  If anywhere in this collection there
	 * is already an element with the same name, an exception will be thrown.
	 *
	 * @param element element to add
	 */
	public void addFirst( ViewElement element ) {
		Assert.notNull( element );

		if ( contains( element.getName() ) ) {
			throw new IllegalArgumentException( "Set already contains a ViewElement with name " + element.getName() );
		}

		elements.addFirst( element );
	}

	/**
	 * Will move an element with a given name from its current position (anywhere in the collection) to the last
	 * position of all children of the new parent.  The parent must be a
	 * {@link com.foreach.across.modules.web.ui.ViewElements} instance.
	 * <p/>
	 * Moving will fail silently if either the element is not found, the parent is not found or the parent
	 * is not of type {@link com.foreach.across.modules.web.ui.ViewElements}.
	 *
	 * @param elementToMove name of the element to move
	 * @param newParent     name of the new parent element
	 * @return true if element was moved, false if it could not be moved for any reason
	 */
	public boolean moveTo( String elementToMove, String newParent ) {
		ViewElements parent = getParent( elementToMove );

		if ( parent != null ) {
			ViewElement other = get( newParent );

			if ( other != null && other instanceof ViewElements ) {
				( (ViewElements) other ).add( parent.remove( elementToMove ) );
				return true;
			}
		}

		return false;
	}

	/**
	 * Will move an element with a given name from its current position (anywhere in the collection) to the first
	 * position of all children of the new parent.  The parent must be a
	 * {@link com.foreach.across.modules.web.ui.ViewElements} instance.
	 * <p/>
	 * Moving will fail silently if either the element is not found, the parent is not found or the parent
	 * is not of type {@link com.foreach.across.modules.web.ui.ViewElements}.
	 *
	 * @param elementToMove name of the element to move
	 * @param newParent     name of the new parent element
	 * @return true if element was moved, false if it could not be moved for any reason
	 */
	public boolean moveToFirst( String elementToMove, String newParent ) {
		ViewElements parent = getParent( elementToMove );

		if ( parent != null ) {
			ViewElement other = get( newParent );

			if ( other != null && other instanceof ViewElements ) {
				( (ViewElements) other ).addFirst( parent.remove( elementToMove ) );
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds all elements directly to this collection.  If any of the elements duplicates the name
	 * of an already present element, an exception will be thrown.
	 *
	 * @param elements elements to add
	 */
	public void addAll( Iterable<ViewElement> elements ) {
		for ( ViewElement element : elements ) {
			add( element );
		}
	}

	/**
	 * Remove the element with that name from wherever it may be in the collection.
	 *
	 * @param elementName name of the element to remove
	 * @return element that was removed - can be null
	 */
	@SuppressWarnings("unchecked")
	public <V extends ViewElement> V remove( String elementName ) {
		ViewElements parent = getParent( elementName );

		if ( parent == this ) {
			ViewElement element = get( elementName );

			if ( element != null ) {
				elements.remove( element );
			}

			return (V) element;
		}
		else {
			return parent.remove( elementName );
		}
	}

	/**
	 * @return true if the collection has no children
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * @return Number of direct children
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * Removes all elements.
	 */
	public void clear() {
		elements.clear();
	}

	/**
	 * @return instance with that name, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public <V extends ViewElement> V get( String elementName ) {
		if ( elementName == null ) {
			return null;
		}

		for ( ViewElement element : elements ) {
			if ( StringUtils.equals( elementName, element.getName() ) ) {
				return (V) element;
			}
			else if ( element instanceof ViewElements ) {
				ViewElement found = ( (ViewElements) element ).get( elementName );

				if ( found != null ) {
					return (V) found;
				}
			}
		}

		return null;
	}

	/**
	 * Searches for the group an element belongs to.  If the element is nowhere in the collection or
	 * it is a direct child of this set, the parent will be null.
	 * <p/>
	 * This will only search top-down and not return any groups that are higher than the current set
	 * (in case the current set is also owned by a
	 * {@link com.foreach.across.modules.web.ui.ViewElements}.
	 *
	 * @param elementName name of the element
	 * @return parent group element or null if direct child or element not found
	 */
	@SuppressWarnings("unchecked")
	public <V extends ViewElements> V getParent( String elementName ) {
		for ( ViewElement element : elements ) {
			if ( StringUtils.equals( elementName, element.getName() ) ) {
				return (V) this;
			}
			else if ( element instanceof ViewElements ) {
				ViewElements parent = ( (ViewElements) element ).getParent( elementName );

				if ( parent != null ) {
					return (V) parent;
				}
			}
		}

		return null;
	}

	/**
	 * @return true if anywhere in the collection there is an element with that name.
	 */
	public boolean contains( String elementName ) {
		return get( elementName ) != null;
	}

	/**
	 * Attempts to sort all elements in this collection according to the order specified by the
	 * element names passed in.  This will sort recursively, if element names are mentioned that
	 * are part of a group, they will be sorted according to the order specified within that group.
	 * <p/>
	 * A group itself will only be moved in relation to its other siblings.
	 *
	 * @param elementNames Collection of element names in order.
	 */
	public void sort( String... elementNames ) {
		final Map<String, Long> newIndices = new HashMap<>();

		long index = 0;

		for ( ViewElement element : elements ) {
			newIndices.put( element.getName(), Integer.MAX_VALUE + index++ );

			if ( element instanceof ViewElements ) {
				( (ViewElements) element ).sort( elementNames );
			}
		}

		index = 0;
		for ( String elementName : elementNames ) {
			newIndices.put( elementName, index++ );
		}

		Collections.sort( elements, new Comparator<ViewElement>()
		{
			@Override
			public int compare( ViewElement left, ViewElement right ) {
				Long leftIndex = newIndices.get( left.getName() );
				Long rightIndex = newIndices.get( right.getName() );

				return leftIndex.compareTo( rightIndex );
			}
		} );
	}

	/**
	 * @return iterator for all direct children in this collection
	 */
	@Override
	public Iterator<ViewElement> iterator() {
		return elements.iterator();
	}
}
