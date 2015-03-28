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

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementCollection;

import java.util.*;

/**
 * Represents a collection of {@link com.foreach.across.modules.web.ui.ViewElement} instances that are
 * generated based on a template or callback method and an input item.  The special case about this
 * implementation is that the elements are only generated once they are requested using the {@link #iterator()},
 * whereas a normal {@link com.foreach.across.modules.web.ui.elements.ContainerViewElement} requires all members
 * to be set before hand.
 * <p>
 * Use this class if you want to configure a template in the form of either a
 * {@link com.foreach.across.modules.web.ui.ViewElement} or {@link com.foreach.across.modules.web.ui.ViewElementBuilder}
 * and provide a set of domain objects for which the template should be customized.  It is generally safer to use a
 * {@link com.foreach.across.modules.web.ui.ViewElementBuilder} as that will always create new
 * {@link com.foreach.across.modules.web.ui.ViewElement} instances instead of modifying the existing one.</p>
 * <p>
 * <strong>If a {@link com.foreach.across.modules.web.ui.ViewElement} is used as item template, the generator
 * is not thread-safe and the same element will be run and possibly modified through every iteration.</strong>
 * </p>
 * <p>Exactly one {@link com.foreach.across.modules.web.ui.ViewElement} will be generated for every domain object.  It is
 * allowed for the generated element to be null - which will cause it not be rendered.  However please be aware that
 * the item will still be present in the iterator and {@link #size()} and {@link #isEmpty()} calls will consider those
 * as valid elements.</p>
 * <p>Like the {@link com.foreach.across.modules.web.ui.elements.ContainerViewElement}, a ViewElementGenerator does not
 * add any additional output, it only renders its children.</p>
 */
public class ViewElementGenerator<T, U extends ViewElement> implements ViewElementCollection<U>, ViewElement
{
	/**
	 * Callback interface for customizing the generated {@link com.foreach.across.modules.web.ui.ViewElement}.
	 *
	 * @param <T> Data item that the callback supports.
	 * @param <U> {@link com.foreach.across.modules.web.ui.ViewElement} type that will be generated
	 */
	public static interface CreationCallback<T, U extends ViewElement>
	{
		/**
		 * Called when the initial {@link com.foreach.across.modules.web.ui.ViewElement} based on the item template
		 * has been generated, allowing customization.
		 *
		 * @param item     Data item for which the view element is generated.
		 * @param template Generated view element based on the template, can be null if no template.
		 * @return ViewElement that should be used.
		 */
		U create( T item, U template );
	}

	private class SingleGenerationIterator implements Iterator<U>
	{
		private int position = 0;
		private List<T> items;
		private List<U> generated;
		private CreationCallback<T, U> callback;

		public SingleGenerationIterator( List<T> items,
		                                 List<U> generated,
		                                 CreationCallback<T, U> callback ) {
			this.items = items;
			this.generated = generated;
			this.callback = callback;
		}

		@Override
		public boolean hasNext() {
			return position < items.size();
		}

		@Override
		public U next() {
			if ( generated.size() <= position ) {
				generated.add( callback.create( items.get( position ), null ) );
			}

			return generated.get( position++ );
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException( "Remove is not supported on generation iterators." );
		}
	}

	/**
	 * Will not store the generated instances.  Used when the template is the same ViewElement that gets modified
	 * on every iteration.
	 */
	private class RepeatedGenerationIterator implements Iterator<U>
	{
		private int position = 0;
		private List<T> items;
		private CreationCallback<T, U> callback;

		public RepeatedGenerationIterator( List<T> items, CreationCallback<T, U> callback ) {
			this.items = items;
			this.callback = callback;
		}

		@Override
		public boolean hasNext() {
			return position < items.size();
		}

		@Override
		public U next() {
			return callback.create( items.get( position++ ), null );
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException( "Remove is not supported on generation iterators." );
		}
	}

	private class ViewElementTemplateCallback implements CreationCallback<T, U>
	{
		private final U template;
		private final CreationCallback<T, U> callback;

		@SuppressWarnings("unchecked")
		public ViewElementTemplateCallback( U template, CreationCallback<T, U> callback ) {
			this.template = template;
			this.callback = callback;
		}

		@Override
		public U create( T item, U original ) {
			if ( callback != null ) {
				return callback.create( item, template );
			}

			return template;
		}
	}

	private class ViewElementBuilderTemplateCallback implements CreationCallback<T, U>
	{
		private final ViewElementBuilder<U> template;
		private final CreationCallback<T, U> callback;

		@SuppressWarnings("unchecked")
		public ViewElementBuilderTemplateCallback( ViewElementBuilder<U> template, CreationCallback<T, U> callback ) {
			this.template = template;
			this.callback = callback;
		}

		@Override
		public U create( T item, U original ) {
			U created = template.build( null );

			if ( callback != null ) {
				return callback.create( item, created );
			}

			return created;
		}
	}

	public static final String ELEMENT_TYPE = StandardViewElements.GENERATOR;

	private List<T> items = Collections.emptyList();
	private List<U> generated = Collections.emptyList();
	private CreationCallback<T, U> callback;

	private Object itemTemplate;

	private String name, customTemplate, elementType;

	public ViewElementGenerator() {
		setElementType( ELEMENT_TYPE );
	}

	public ViewElementGenerator( String name ) {
		this.name = name;
	}

	public void setItems( Collection<T> items ) {
		this.items = new ArrayList<>( items );
		generated = new ArrayList<>( items.size() );
	}

	public void setItemTemplate( U element ) {
		this.itemTemplate = element;
	}

	public void setItemTemplate( ViewElementBuilder<U> builder ) {
		this.itemTemplate = builder;
	}

	/**
	 * @return itemTemplate if it is a {@link com.foreach.across.modules.web.ui.ViewElement}, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public U getItemTemplateAsElement() {
		return (U) itemTemplate;
	}

	/**
	 * @return itemTemplate if it is a {@link com.foreach.across.modules.web.ui.ViewElementBuilder}, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public ViewElementBuilder<U> getItemTemplateAsBuilder() {
		return (ViewElementBuilder<U>) itemTemplate;
	}

	/**
	 * @return true if the item template is a {@link com.foreach.across.modules.web.ui.ViewElementBuilder}
	 */
	public boolean isBuilderItemTemplate() {
		return itemTemplate instanceof ViewElementBuilder;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public String getCustomTemplate() {
		return customTemplate;
	}

	public void setCustomTemplate( String customTemplate ) {
		this.customTemplate = customTemplate;
	}

	@Override
	public String getElementType() {
		return elementType;
	}

	protected void setElementType( String elementType ) {
		this.elementType = elementType;
	}

	/**
	 * Method to be called once for every generation of a {@link com.foreach.across.modules.web.ui.ViewElement}.
	 * If a template is specified, the callback will happen after the initial
	 * {@link com.foreach.across.modules.web.ui.ViewElement} based on the template has been generated.
	 *
	 * @param callback implementation
	 */
	public void setCreationCallback( CreationCallback<T, U> callback ) {
		this.callback = callback;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public Iterator<U> iterator() {
		if ( isBuilderItemTemplate() ) {
			return new SingleGenerationIterator(
					items, generated, new ViewElementBuilderTemplateCallback( getItemTemplateAsBuilder(), callback )
			);
		}

		return new RepeatedGenerationIterator(
				items, new ViewElementTemplateCallback( getItemTemplateAsElement(), callback )
		);
	}
}
