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

import com.foreach.across.modules.web.ui.*;

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
public class ViewElementGenerator<ITEM, VIEW_ELEMENT extends ViewElement> implements ViewElementCollection<VIEW_ELEMENT>, MutableViewElement
{
	/**
	 * Callback interface for customizing the generated {@link com.foreach.across.modules.web.ui.ViewElement}.
	 *
	 * @param <T> Data item that the callback supports.
	 * @param <U> {@link com.foreach.across.modules.web.ui.ViewElement} type that will be generated
	 */
	public interface CreationCallback<T, U extends ViewElement>
	{
		/**
		 * Called when the initial {@link com.foreach.across.modules.web.ui.ViewElement} based on the item template
		 * has been generated, allowing customization.
		 *
		 * @param item     Data item and stats for which the view element is generated.
		 * @param template Generated view element based on the template, can be null if no template.
		 * @return ViewElement that should be used.
		 */
		U create( IteratorItemStats<T> item, U template );
	}

	private class SingleGenerationIterator implements Iterator<VIEW_ELEMENT>
	{
		private int position = 0;
		private List<ITEM> items;
		private List<VIEW_ELEMENT> generated;
		private CreationCallback<ITEM, VIEW_ELEMENT> callback;

		public SingleGenerationIterator( List<ITEM> items,
		                                 List<VIEW_ELEMENT> generated,
		                                 CreationCallback<ITEM, VIEW_ELEMENT> callback ) {
			this.items = items;
			this.generated = generated;
			this.callback = callback;
		}

		@Override
		public boolean hasNext() {
			return position < items.size();
		}

		@Override
		public VIEW_ELEMENT next() {
			if ( generated.size() <= position ) {
				ITEM item = items.get( position );

				IteratorItemStats<ITEM> itemStats = new IteratorItemStatsImpl<>( item, position,
				                                                                 position < items.size() );
				IteratorViewElementBuilderContext<ITEM> ctx = new IteratorViewElementBuilderContext<>( itemStats );
				if ( itemBuilderContext != null ) {
					ctx.setParentContext( itemBuilderContext );
				}

				ViewElementBuilder<VIEW_ELEMENT> builder = getItemTemplateAsBuilder();
				VIEW_ELEMENT element = builder.build( ctx );

				if ( callback != null ) {
					element = callback.create( itemStats, element );
				}

				generated.add( element );
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
	private class RepeatedGenerationIterator implements Iterator<VIEW_ELEMENT>
	{
		private int position = 0;
		private List<ITEM> items;
		private CreationCallback<ITEM, VIEW_ELEMENT> callback;

		public RepeatedGenerationIterator( List<ITEM> items, CreationCallback<ITEM, VIEW_ELEMENT> callback ) {
			this.items = items;
			this.callback = callback;
		}

		@Override
		public boolean hasNext() {
			return position < items.size();
		}

		@Override
		public VIEW_ELEMENT next() {
			IteratorItemStats<ITEM> itemStats = new IteratorItemStatsImpl<>( items.get( position ), position,
			                                                                 position++ < items.size() );

			return callback != null
					? callback.create( itemStats, getItemTemplateAsElement() ) : getItemTemplateAsElement();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException( "Remove is not supported on generation iterators." );
		}
	}

	public static final String ELEMENT_TYPE = StandardViewElements.GENERATOR;

	private List<ITEM> items = Collections.emptyList();
	private List<VIEW_ELEMENT> generated = Collections.emptyList();
	private CreationCallback<ITEM, VIEW_ELEMENT> callback;

	private Object itemTemplate;
	private ViewElementBuilderContext itemBuilderContext;

	private String name, customTemplate, elementType;

	public ViewElementGenerator() {
		setElementType( ELEMENT_TYPE );
	}

	public ViewElementGenerator( String name ) {
		this.name = name;
	}

	public void setItems( Collection<ITEM> items ) {
		this.items = new ArrayList<>( items );
		generated = new ArrayList<>( items.size() );
	}

	public void setItemTemplate( VIEW_ELEMENT element ) {
		this.itemTemplate = element;
	}

	public void setItemTemplate( ViewElementBuilder<VIEW_ELEMENT> builder ) {
		this.itemTemplate = builder;
	}

	/**
	 * Set the {@link ViewElementBuilderContext} that should be used when generating the {@link ViewElement} using
	 * a {@link ViewElementBuilder} as item template.  This context will serve as the parent context for the
	 * {@link IteratorViewElementBuilderContext} that will be passed to the item template builder.
	 *
	 * @param itemBuilderContext that contains the attributes that should be available to the builder
	 */
	public void setItemBuilderContext( ViewElementBuilderContext itemBuilderContext ) {
		this.itemBuilderContext = itemBuilderContext;
	}

	public ViewElementBuilderContext getItemBuilderContext() {
		return itemBuilderContext;
	}

	/**
	 * @return itemTemplate if it is a {@link com.foreach.across.modules.web.ui.ViewElement}, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public VIEW_ELEMENT getItemTemplateAsElement() {
		return (VIEW_ELEMENT) itemTemplate;
	}

	/**
	 * @return itemTemplate if it is a {@link com.foreach.across.modules.web.ui.ViewElementBuilder}, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public ViewElementBuilder<VIEW_ELEMENT> getItemTemplateAsBuilder() {
		return (ViewElementBuilder<VIEW_ELEMENT>) itemTemplate;
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
	public void setCreationCallback( CreationCallback<ITEM, VIEW_ELEMENT> callback ) {
		this.callback = callback;
	}

	public CreationCallback<ITEM, VIEW_ELEMENT> getCreationCallback() {
		return callback;
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
	public Iterator<VIEW_ELEMENT> iterator() {
		if ( isBuilderItemTemplate() ) {
			return new SingleGenerationIterator( items, generated, callback );
		}

		return new RepeatedGenerationIterator( items, callback );
	}
}
