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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.*;
import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author Arne Vandamme
 */
public class ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT extends ViewElement>
		extends ViewElementBuilderSupport<ViewElementGenerator<ITEM, VIEW_ELEMENT>, ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT>>
{
	private Collection<ITEM> items;
	private ViewElementBuilder<VIEW_ELEMENT> itemBuilder;
	private ViewElementGenerator.CreationCallback<ITEM, VIEW_ELEMENT> creationCallback;

	/**
	 * Set the collection of items for which elements should be generated.
	 *
	 * @param items collection
	 * @return current builder
	 */
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> items( Collection<ITEM> items ) {
		this.items = items;
		return this;
	}

	/**
	 * Set the callback to be executed after an item has been created by the {@link ViewElementGenerator}.
	 *
	 * @param callback instance
	 * @return current builder
	 */
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> creationCallback(
			ViewElementGenerator.CreationCallback<ITEM, VIEW_ELEMENT> callback ) {
		this.creationCallback = callback;
		return this;
	}

	/**
	 * Set the {@link ViewElementBuilder} to call when generating an item.
	 *
	 * @param itemBuilder instance
	 * @return current builder
	 */
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> itemBuilder( ViewElementBuilder<VIEW_ELEMENT> itemBuilder ) {
		this.itemBuilder = itemBuilder;
		return this;
	}

	@Override
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> name( String name ) {
		return super.name( name );
	}

	@Override
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> customTemplate( String template ) {
		return super.customTemplate( template );
	}

	@Override
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> configure( Consumer<ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT>> consumer ) {
		return super.configure( consumer );
	}

	@Override
	public ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> postProcessor( ViewElementPostProcessor<ViewElementGenerator<ITEM, VIEW_ELEMENT>> postProcessor ) {
		return super.postProcessor( postProcessor );
	}

	@Override
	protected ViewElementGenerator<ITEM, VIEW_ELEMENT> createElement( ViewElementBuilderContext builderContext ) {
		ViewElementGenerator<ITEM, VIEW_ELEMENT> generator = new ViewElementGenerator<>();
		apply( generator, builderContext );

		if ( itemBuilder != null ) {
			generator.setItemTemplate( itemBuilder );
		}

		if ( items != null ) {
			generator.setItems( items );
		}

		if ( creationCallback != null ) {
			generator.setCreationCallback( creationCallback );
		}

		generator.setItemBuilderContext( builderContext );

		return generator;
	}
}
