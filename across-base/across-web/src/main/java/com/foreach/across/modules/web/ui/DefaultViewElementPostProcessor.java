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

/**
 * <p>Implementation that looks for a collection of {@link ViewElementPostProcessor}s on the
 * {@link ViewElementBuilderContext} and applies all of them.  All {@link ViewElementBuilderSupport}
 * instances will automatically execute this post processor when building.</p>
 * <p>The easiest way to add a default {@link ViewElementPostProcessor} is to use the static
 * {@link #add(ViewElementBuilderContext, ViewElementPostProcessor)} method.</p>
 *
 * @author Arne Vandamme
 */
public final class DefaultViewElementPostProcessor<T extends ViewElement> implements ViewElementPostProcessor<T>
{
	public static final DefaultViewElementPostProcessor INSTANCE = new DefaultViewElementPostProcessor();

	/**
	 * Name of the attribute that contains the {@link java.util.Collection} holding the post processors.
	 */
	public static final String ATTRIBUTE_NAME = DefaultViewElementPostProcessor.class.getName() + ".members";

	private DefaultViewElementPostProcessor() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void postProcess( ViewElementBuilderContext builderContext, ViewElement element ) {
		Collection<ViewElementPostProcessor> members
				= (Collection<ViewElementPostProcessor>) builderContext.getAttribute( ATTRIBUTE_NAME,
				                                                                      Collection.class );

		if ( members != null ) {
			members.forEach( postProcessor -> postProcessor.postProcess( builderContext, element ) );
		}
	}

	/**
	 * Add an additional post processor to the {@link ViewElementBuilderContext}.  This post processor will be
	 * executed by all {@link ViewElementBuilder}s that are executed in the builder context and have the
	 * {@link DefaultViewElementPostProcessor#INSTANCE} configured.
	 *
	 * @param builderContext in which to register the post processor
	 * @param postProcessor  instance
	 */
	@SuppressWarnings("unchecked")
	public static void add( ViewElementBuilderContext builderContext, ViewElementPostProcessor<?> postProcessor ) {
		Collection<ViewElementPostProcessor> members
				= (Collection<ViewElementPostProcessor>) builderContext.getAttribute( ATTRIBUTE_NAME,
				                                                                      Collection.class );

		if ( members == null ) {
			members = new ArrayList<>();
			builderContext.setAttribute( ATTRIBUTE_NAME, members );
		}

		members.add( postProcessor );
	}

	/**
	 * Remove a registered post processor from the builder context.
	 *
	 * @param builderContext from which to remove the post processor
	 * @param postProcessor  instance that should be removed
	 * @return true if the post processor was found
	 */
	@SuppressWarnings("unchecked")
	public static boolean remove( ViewElementBuilderContext builderContext, ViewElementPostProcessor postProcessor ) {
		Collection<ViewElementPostProcessor> members
				= (Collection<ViewElementPostProcessor>) builderContext.getAttribute( ATTRIBUTE_NAME,
				                                                                      Collection.class );

		return members != null && members.remove( postProcessor );
	}
}
