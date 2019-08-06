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
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Arne Vandamme
 * @since 3.3.0
 */
public class ContainerLayoutViewElementPostProcessor<T extends ContainerViewElement> implements ViewElementPostProcessor<T>
{
	private final List<LayoutRule<T>> rules = new ArrayList<>();
	private final Deque<Map<LayoutRule, Object>> ruleData = new ArrayDeque<>();

	public MoveLayoutRule move( String... sources ) {
		return addRule( new MoveLayoutRule( toReferences( sources ) ) );
	}

	public AddLayoutRule add( ViewElementBuilder builder ) {
		return addRule( new AddLayoutRule( builder ) );
	}

	public RemoveLayoutRule remove( String... sources ) {
		return addRule( new RemoveLayoutRule( toReferences( sources ) ) );
	}

	private List<ViewElementReference<T>> toReferences( String... sources ) {
		return Stream.of( sources ).map( ByNameViewElementReference::new ).collect( Collectors.toList() );
	}

	private <U extends LayoutRule<T>> U addRule( U rule ) {
		rules.add( rule );
		return rule;
	}

	@Override
	public void postProcess( ViewElementBuilderContext builderContext, T element ) {
		ruleData.push( new IdentityHashMap<>() );

		try {
			rules.forEach( r -> r.apply( builderContext, element ) );
		}
		finally {
			ruleData.pop();
		}
	}

	private Map<LayoutRule, Object> data() {
		return ruleData.peek();
	}

	private interface ViewElementReference<T>
	{
		ViewElement resolve( T element );
	}

	@RequiredArgsConstructor
	private class ByNameViewElementReference implements ViewElementReference<T>
	{
		private final String elementName;

		@Override
		public ViewElement resolve( T element ) {
			return element.removeFromTree( elementName ).orElse( null );
		}
	}

	private interface LayoutRule<T>
	{
		void apply( ViewElementBuilderContext builderContext, T element );
	}

	@RequiredArgsConstructor
	public class AddLayoutRule implements LayoutRule<T>
	{
		private final ViewElementBuilder builder;

		@Override
		public void apply( ViewElementBuilderContext builderContext, T element ) {
			ViewElement elementToAdd = builder.build( builderContext );
			if ( elementToAdd != null ) {
				element.addChild( elementToAdd );
			}
		}
	}

	@RequiredArgsConstructor
	public class RemoveLayoutRule implements LayoutRule<T>, ViewElementBuilder
	{
		private final List<ViewElementReference<T>> sources;

		@Override
		public void apply( ViewElementBuilderContext builderContext, T element ) {
			List<ViewElement> removed = new ArrayList<>();
			sources.stream()
			       .map( ref -> ref.resolve( element ) )
			       .filter( Objects::nonNull )
			       .forEach( removed::add );
			data().put( this, removed );
		}

		@Override
		public ViewElement build( ViewElementBuilderContext builderContext ) {
			List<ViewElement> removed = (List<ViewElement>) data().get( this );

			ContainerViewElement elements = new ContainerViewElement();

			if ( removed != null && !removed.isEmpty() ) {
				if ( removed.size() == 1 ) {
					return removed.get( 0 );
				}

				elements.addChildren( removed );
			}

			return elements;
		}
	}

	@RequiredArgsConstructor
	public class MoveLayoutRule implements LayoutRule<T>
	{
		private final List<ViewElementReference<T>> sources;
		private String target;

		public ContainerLayoutViewElementPostProcessor<T> to( String target ) {
			this.target = target;
			return ContainerLayoutViewElementPostProcessor.this;
		}

		@Override
		public void apply( ViewElementBuilderContext builderContext, T element ) {
			element.find( target, ContainerViewElement.class )
			       .ifPresent( t -> {
				       sources.stream()
				              .map( ref -> ref.resolve( element ) )
				              .filter( Objects::nonNull )
				              .forEach( t::addChild );
			       } );
		}
	}
}
