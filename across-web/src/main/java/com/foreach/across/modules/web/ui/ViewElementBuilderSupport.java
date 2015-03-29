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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ViewElementBuilderSupport<T extends ViewElement, SELF extends ViewElementBuilder>
		implements ViewElementBuilder<T>
{
	/**
	 * Encapsulates either a {@link ViewElement} or {@link ViewElementBuilder} for fetching within a
	 * {@link ViewElementBuilderContext}.
	 */
	public static class ElementOrBuilder
	{
		private final Object elementOrBuilder;

		protected ElementOrBuilder( Object elementOrBuilder ) {
			this.elementOrBuilder = elementOrBuilder;
		}

		public ViewElement get( ViewElementBuilderContext builderContext ) {
			if ( elementOrBuilder instanceof ViewElement ) {
				return (ViewElement) elementOrBuilder;
			}

			if ( elementOrBuilder instanceof ViewElementBuilder ) {
				return ( (ViewElementBuilder) elementOrBuilder ).build( builderContext );
			}

			return null;
		}

		public static ElementOrBuilder wrap( ViewElement viewElement ) {
			return new ElementOrBuilder( viewElement );
		}

		public static ElementOrBuilder wrap( ViewElementBuilder builder ) {
			return new ElementOrBuilder( builder );
		}

		public static Collection<ElementOrBuilder> wrap( ViewElement... viewElements ) {
			List<ElementOrBuilder> wrapped = new ArrayList<>( viewElements.length );
			for ( ViewElement viewElement : viewElements ) {
				wrapped.add( new ElementOrBuilder( viewElement ) );
			}

			return wrapped;
		}

		public static Collection<ElementOrBuilder> wrap( ViewElementBuilder... viewElementBuilders ) {
			List<ElementOrBuilder> wrapped = new ArrayList<>( viewElementBuilders.length );
			for ( ViewElementBuilder builder : viewElementBuilders ) {
				wrapped.add( new ElementOrBuilder( builder ) );
			}

			return wrapped;
		}
	}

	protected String name, customTemplate;

	@SuppressWarnings("unchecked")
	public SELF name( String name ) {
		this.name = name;
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF customTemplate( String template ) {
		this.customTemplate = template;
		return (SELF) this;
	}

	protected final <V extends MutableViewElement> V apply( V viewElement ) {
		if ( name != null ) {
			viewElement.setName( name );
		}
		if ( customTemplate != null ) {
			viewElement.setCustomTemplate( customTemplate );
		}

		return viewElement;
	}
}
