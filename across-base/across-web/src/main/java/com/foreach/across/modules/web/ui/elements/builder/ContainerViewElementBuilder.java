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
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContainerViewElementBuilder extends ContainerViewElementBuilderSupport<ContainerViewElement, ContainerViewElementBuilder>
{
	private final List<ElementOrBuilder> children = new ArrayList<>();
	private String[] sortElements;

	@Override
	public ContainerViewElementBuilder addFirst( ViewElement... viewElements ) {
		Stream.of( viewElements ).filter( Objects::nonNull ).forEach( e -> children.add( 0, ElementOrBuilder.wrap( e ) ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder addFirst( ViewElementBuilder... viewElements ) {
		Stream.of( viewElements ).filter( Objects::nonNull ).forEach( e -> children.add( 0, ElementOrBuilder.wrap( e ) ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder add( ViewElement... viewElements ) {
		children.addAll( ElementOrBuilder.wrap( Arrays.asList( viewElements ) ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder add( ViewElementBuilder... viewElements ) {
		children.addAll( ElementOrBuilder.wrap( viewElements ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder addAll( Iterable<?> viewElements ) {
		children.addAll( ElementOrBuilder.wrap( viewElements ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder sort( String... elementNames ) {
		this.sortElements = elementNames;
		return this;
	}

	@Override
	protected ContainerViewElement createElement( ViewElementBuilderContext builderContext ) {
		ContainerViewElement container = new ContainerViewElement();

		for ( ElementOrBuilder child : children ) {
			container.addChild( child.get( builderContext ) );
		}

		if ( sortElements != null ) {
			ContainerViewElementUtils.sortRecursively( container, sortElements );
		}

		return apply( container, builderContext );
	}
}
