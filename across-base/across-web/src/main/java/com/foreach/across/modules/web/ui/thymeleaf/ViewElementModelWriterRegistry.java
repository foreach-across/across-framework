/*
 * Copyright 2019 the original author or authors
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
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ViewElementModelWriterRegistry
{
	private final Map<Object, ViewElementModelWriter> modelWriters = new HashMap<>();

	public void registerModelWriter( Class<? extends ViewElement> viewElementClass,
	                                 ViewElementModelWriter builder ) {
		modelWriters.put( viewElementClass, builder );
	}

	public void registerModelWriter( String viewElementType, ViewElementModelWriter builder ) {
		modelWriters.put( viewElementType, builder );
	}

	/**
	 * Finds the most appropriate {@link ViewElementModelWriter}: first attempts to find a writer
	 * registered to that specific class, second looks for the element type.
	 *
	 * @param viewElement instance for which to find a builder
	 * @return builder instance or null if none found
	 */
	@SuppressWarnings( "unchecked" )
	public ViewElementModelWriter<ViewElement> getModelWriter( @NonNull ViewElement viewElement ) {
		ViewElementModelWriter builder = modelWriters.get( viewElement.getClass() );

		return builder != null ? builder : modelWriters.get( viewElement.getElementType() );
	}
}
