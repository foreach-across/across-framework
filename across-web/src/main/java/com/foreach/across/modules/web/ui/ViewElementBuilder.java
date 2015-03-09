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

/**
 * Base interface to create a single {@link ViewElement} instance.
 *
 * @author Arne Vandamme
 */
public interface ViewElementBuilder<T extends ViewElement>
{
	/**
	 * Builds the actual element.
	 *
	 * @param builderContext provides the context for this build event
	 * @return instance to render the element.
	 */
	T build( ViewElementBuilderContext builderContext );
}
