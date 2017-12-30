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

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class ContainerViewElementBuilderSupport<T extends ContainerViewElement, SELF extends ContainerViewElementBuilderSupport<T, SELF>>
		extends ViewElementBuilderSupport<T, SELF>
{
	private final List<Object> children = new ArrayList<>();
	private String[] sortElements;

	@SuppressWarnings("unchecked")
	public SELF addFirst( ViewElement... viewElements ) {
		Stream.of( viewElements ).forEach( e -> children.add( 0, e ) );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF addFirst( ViewElementBuilder... viewElements ) {
		Stream.of( viewElements ).forEach( e -> children.add( 0, e ) );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF add( ViewElement... viewElements ) {
		Collections.addAll( children, viewElements );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF add( ViewElementBuilder... viewElements ) {
		Collections.addAll( children, viewElements );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF addAll( Iterable<?> viewElements ) {
		for ( Object viewElement : viewElements ) {
			Assert.isTrue( viewElement instanceof ViewElement || viewElement instanceof ViewElementBuilder,
			               "viewElement should be an instance of ViewElement of ViewElementBuilder" );
			children.add( viewElement );
		}
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF sort( String... elementNames ) {
		this.sortElements = elementNames;
		return (SELF) this;
	}

	@Override
	protected T apply( T viewElement, ViewElementBuilderContext builderContext ) {
		T container = super.apply( viewElement, builderContext );

		for ( Object child : children ) {
			if ( child != null ) {
				if ( child instanceof ViewElement ) {
					container.addChild( (ViewElement) child );
				}
				else {
					container.addChild( ( (ViewElementBuilder) child ).build( builderContext ) );
				}
			}
		}

		if ( sortElements != null ) {
			ContainerViewElementUtils.sortRecursively( container, sortElements );
		}

		return container;
	}
}
