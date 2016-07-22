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
package com.foreach.across.modules.web.ui.elements.support;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Contains a set of utility functions for manipulating {@link ContainerViewElement}s recursively.
 * Allows traversal of the entire tree of children in a top-down fashion, and manipulations like remove and replace.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public final class ContainerViewElementUtils
{
	private ContainerViewElementUtils() {
	}

	/**
	 * Find the first element with the given name in the container.  Will recursive through all children
	 * that are also {@link ContainerViewElement} implementations.  Search will be top-down and the first
	 * matching element will be returned.
	 *
	 * @param container   collection of elements
	 * @param elementName name of the element
	 * @return element if found
	 */
	public static Optional<ViewElement> find( ContainerViewElement container,
	                                          String elementName ) {
		return find( container, elementName, ViewElement.class );
	}

	/**
	 * Find the first element with the given name that is also of the required type.
	 * Will recursive through all children that are also {@link ContainerViewElement} implementations.
	 * Search will be top-down and the first matching element will be returned.  If no element has that
	 * name or it is not of the required type, {@link Optional#empty()} will be returned.
	 *
	 * @param container    collection of elements
	 * @param elementName  name of the element
	 * @param requiredType type the element should have
	 * @return element if found
	 */
	public static <V extends ViewElement> Optional<V> find( ContainerViewElement container,
	                                                        String elementName,
	                                                        Class<V> requiredType ) {
		if ( elementName == null ) {
			return Optional.empty();
		}

		for ( ViewElement element : container.getChildren() ) {
			if ( StringUtils.equals( elementName, element.getName() ) && requiredType.isInstance( element ) ) {
				return Optional.of( requiredType.cast( element ) );
			}
			else if ( element instanceof ContainerViewElement ) {
				Optional<V> foundInChild = find( (ContainerViewElement) element, elementName, requiredType );

				if ( foundInChild.isPresent() ) {
					return foundInChild;
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Find the parent {@link ContainerViewElement} of the child element.  The original container will be returned
	 * if the element is a direct child.
	 *
	 * @param container collection of elements
	 * @param child     element to find the parent for
	 * @return container or self
	 */
	public static Optional<ContainerViewElement> findParent( ContainerViewElement container, ViewElement child ) {
		if ( child == null ) {
			return Optional.empty();
		}

		for ( ViewElement element : container.getChildren() ) {
			if ( child.equals( element ) ) {
				return Optional.of( container );
			}
			else if ( element instanceof ContainerViewElement ) {
				Optional<ContainerViewElement> foundInChild = findParent( (ContainerViewElement) element, child );

				if ( foundInChild.isPresent() ) {
					return foundInChild;
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Recursively sorts all children of the container according to the order specified
	 * by the element names passed in. If any children are containers they will in turn be sorted.
	 *
	 * @param container    whose children to sort
	 * @param elementNames Collection of element names in order.
	 */
	public static void sortRecursively( ContainerViewElement container, String... elementNames ) {
		sortRecursively( container.getChildren(), elementNames );
	}

	/**
	 * Recursively sorts the list of elements according to the order specified by the element names passed in.
	 * If any children are containers they will in turn be sorted.
	 *
	 * @param viewElements to sort
	 * @param elementNames Collection of element names in order.
	 */
	public static void sortRecursively( List<ViewElement> viewElements, String... elementNames ) {
		sort( viewElements, elementNames );

		viewElements.stream()
		            .filter( ContainerViewElement.class::isInstance )
		            .forEach( e -> sort( (ContainerViewElement) e, elementNames ) );
	}

	/**
	 * Sorts the direct children of this container according to the element names passed in.
	 *
	 * @param container    whose children to sort
	 * @param elementNames Collection of element names in order.
	 */
	public static void sort( ContainerViewElement container, String... elementNames ) {
		sort( container.getChildren(), elementNames );
	}

	/**
	 * Sorts the list of view elements according to the element names passed in.
	 * Named elements will be put before unnamed elements, but the original order
	 * will be kept in case of doubt.
	 *
	 * @param viewElements collection to sort
	 * @param elementNames Collection of element names in order.
	 */
	public static void sort( List<ViewElement> viewElements, String... elementNames ) {
		Map<ViewElement, Integer> originalIndices = new HashMap<>();
		Map<String, Integer> newIndices = new HashMap<>();

		for ( int i = 0; i < viewElements.size(); i++ ) {
			originalIndices.put( viewElements.get( i ), i );
		}

		int index = 0;
		for ( String elementName : elementNames ) {
			newIndices.put( elementName, index++ );
		}

		Collections.sort(
				viewElements,
				Comparator.comparingInt( element -> newIndices.getOrDefault( element.getName(), Integer.MAX_VALUE ) )
		);
	}

	/**
	 * Remove the first element with the given name from the container or any of its children.
	 * The container will be searched top-down recursively until an element is found. The return value will be empty
	 * if no element with the given name was present.
	 *
	 * @param container   to remove the element from
	 * @param elementName name of the element to remove
	 * @return element that has been remove
	 */
	public static Optional<ViewElement> remove( ContainerViewElement container, String elementName ) {
		Optional<ViewElement> element = find( container, elementName );
		if ( !element.isPresent() || !remove( container, element.get() ) ) {
			return Optional.empty();
		}

		return element;
	}

	/**
	 * Remove the given element from the container.  Will search the container top-down recursively.
	 *
	 * @param container to remove the element from
	 * @param element   to remove
	 * @return true if element was present and has been removed
	 */
	public static boolean remove( ContainerViewElement container, ViewElement element ) {
		Optional<ContainerViewElement> parent = findParent( container, element );
		return parent.isPresent() && parent.get().removeChild( element );

	}

	/**
	 * Attempts to move an element within the container: the element with the given name will be moved to the
	 * parent specified and remove from its current parent.  True will be returned if the element has been removed
	 * from its existing parent (and thus was found and has been added to the new one).
	 * <p>If the new parent already contains the element, it will not be added again, nor will its existing
	 * position in the new parent change.  This does not mean however that no remove action from a different
	 * parent has happened.</p>
	 *
	 * @param container     to move the element in
	 * @param elementToMove name of the element to move
	 * @param newParent     name of the new parent element in the container
	 * @return true if the element has been removed from its previous parent
	 */
	public static boolean move( ContainerViewElement container, String elementToMove, String newParent ) {
		Optional<ViewElement> element = find( container, elementToMove );
		return element.isPresent() && move( container, element.get(), newParent );
	}

	/**
	 * Attempts to move an element in the container to a new parent.  The new parent does not need to be a
	 * member of the container.  True will be returned if the element has been removed from its existing parent
	 * (and thus was found and has been added to the new one).
	 * <p>If the new parent already contains the element, it will not be added again, nor will its existing
	 * position in the new parent change.  This does not mean however that no remove action from a different
	 * parent has happened.</p>
	 *
	 * @param container     to move the element in
	 * @param elementToMove name of the element to move
	 * @param newParent     container to add the element to
	 * @return true if the element has been removed from its previous parent
	 */
	public static boolean move( ContainerViewElement container, String elementToMove, ContainerViewElement newParent ) {
		Optional<ViewElement> element = find( container, elementToMove );
		return element.isPresent() && move( container, element.get(), newParent );
	}

	/**
	 * Attempts to move an element to a new parent that is part of the container.  The actual element does not
	 * need to be a member of the container.  True will be returned if the element has been
	 * removed from its existing parent (and thus was found and has been added to the new one).
	 * <p>If the new parent already contains the element, it will not be added again, nor will its existing
	 * position in the new parent change.  This does not mean however that no remove action from a different
	 * parent has happened.</p>
	 *
	 * @param container     to move the element in
	 * @param elementToMove element to move
	 * @param newParent     name of the new parent element in the container
	 * @return true if the element has been removed from its previous parent
	 */
	public static boolean move( ContainerViewElement container, ViewElement elementToMove, String newParent ) {
		Optional<ContainerViewElement> target = find( container, newParent, ContainerViewElement.class );
		return target.isPresent() && move( container, elementToMove, target.get() );
	}

	/**
	 * Moves an element to a new parent container.  If the element is also a member of the container, it
	 * will be removed from its current parent in the container (provided that current parent is different than
	 * the new one).  True will be returned if the element has been removed from an existing parent in the container.
	 * <p>If the new parent already contains the element, it will not be added again, nor will its existing
	 * position in the new parent change.  This does not mean however that no remove action from a different
	 * parent has happened.</p>
	 *
	 * @param container     to move the element in
	 * @param elementToMove element to move
	 * @param newParent     container to add the element to
	 * @return true if the element has been removed from its previous parent
	 */
	public static boolean move( ContainerViewElement container,
	                            ViewElement elementToMove,
	                            ContainerViewElement newParent ) {
		Optional<ContainerViewElement> existingParent = findParent( container, elementToMove );

		// remove from current parent if current is not the same as new
		boolean removed = false;

		if ( existingParent.isPresent() && existingParent.get() != newParent ) {
			existingParent.get().removeChild( elementToMove );
			removed = true;
		}

		// add to new parent if necessary
		if ( !newParent.getChildren().contains( elementToMove ) ) {
			newParent.addChild( elementToMove );
		}

		return removed;
	}

	/**
	 * Replace an element in the container hierarchy by another element.  If the element to replace is not
	 * foundnothing will happen.  If the element is found, it will be passed  as an argument to the
	 * replacementFunction parameter.  The return value will be the replacement element.
	 * If the replacement element is {@code null}, this operation is the same as
	 * a {@link #remove(ContainerViewElement, ViewElement)}.
	 *
	 * @param container           to replace the element in
	 * @param elementName         name of the element to replace
	 * @param replacementFunction function that takes the original element as parameter and returns
	 *                            the replacement element  (or {@code null}
	 * @return true if element was found and has been replaced (or removed)
	 */
	public static boolean replace( ContainerViewElement container,
	                               String elementName,
	                               UnaryOperator<ViewElement> replacementFunction ) {
		return replace( container, elementName, ViewElement.class, replacementFunction );
	}

	/**
	 * Replace an element in the container hierarchy by another element.  If the element to replace is not
	 * found or is not of the required type, nothing will happen.  If the element is found, it will be passed
	 * as an argument to the replacementFunction parameter.  The return value will be the replacement element.
	 * If the replacement element is {@code null}, this operation is the same as
	 * a {@link #remove(ContainerViewElement, ViewElement)}.
	 *
	 * @param container           to replace the element in
	 * @param elementName         name of the element to replace
	 * @param requiredType        type the original element is expected to have
	 * @param replacementFunction function that takes the original element as parameter and returns
	 *                            the replacement element  (or {@code null}
	 * @return true if element was found and has been replaced (or removed)
	 */
	public static <V extends ViewElement> boolean replace( ContainerViewElement container,
	                                                       String elementName,
	                                                       Class<V> requiredType,
	                                                       Function<V, ? extends ViewElement> replacementFunction ) {
		Optional<V> elementToReplace = find( container, elementName, requiredType );

		if ( elementToReplace.isPresent() ) {
			ViewElement replacement = replacementFunction.apply( elementToReplace.get() );
			return replace( container, elementToReplace.get(), replacement );
		}

		return false;
	}

	/**
	 * Replace an element in the container hierarchy by another element.  If the element to replace is not
	 * a child of the container (or any of its children) nothing will happen.  If the replacement element
	 * is {@code null}, this operation is the same as a {@link #remove(ContainerViewElement, ViewElement)}.
	 *
	 * @param container        to replace the element in
	 * @param elementToReplace element to replace
	 * @param replacement      new element that should be put in the same position
	 * @return true if element was found and has been replaced (or removed)
	 */
	public static boolean replace( ContainerViewElement container,
	                               ViewElement elementToReplace,
	                               ViewElement replacement ) {
		Optional<ContainerViewElement> parent = findParent( container, elementToReplace );

		if ( parent.isPresent() ) {
			List<ViewElement> children = parent.get().getChildren();
			int index = children.indexOf( elementToReplace );

			if ( index >= 0 ) {
				if ( replacement != null ) {
					children.set( index, replacement );
				}
				else {
					children.remove( index );
				}
				return true;
			}
		}

		return false;
	}
}
