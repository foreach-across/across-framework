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
package com.foreach.across.modules.web.ui.thymeleaf;

import com.foreach.across.modules.web.ui.ViewElement;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Service
public class ViewElementNodeBuilderRegistry
{
	private final Map<Object, ViewElementNodeBuilder> builders = new HashMap<>();

	public void registerNodeBuilder( Class<? extends ViewElement> viewElementClass,
	                                 ViewElementNodeBuilder builder ) {
		builders.put( viewElementClass, builder );
	}

	public void registerNodeBuilder( String viewElementType, ViewElementNodeBuilder builder ) {
		builders.put( viewElementType, builder );
	}

	/**
	 * Finds the most appropriate {@link ViewElementNodeBuilder}: first attempts to find a builder
	 * registered to that specific class, second looks for the element type.
	 *
	 * @param viewElement instance for which to find a builder
	 * @return builder instance or null if none found
	 */
	public ViewElementNodeBuilder getNodeBuilder( ViewElement viewElement ) {
		Assert.notNull( viewElement );
		ViewElementNodeBuilder builder = builders.get( viewElement.getClass() );

		return builder != null ? builder : builders.get( viewElement.getElementType() );
	}
}
