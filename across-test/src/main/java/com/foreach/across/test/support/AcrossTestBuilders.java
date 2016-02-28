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
package com.foreach.across.test.support;

/**
 * Helper for creating relevant {@link com.foreach.across.test.AcrossTestContext} builders.
 * Can be used as static imports.
 * <p>
 * Example: using {@link AcrossTestBuilders} as a static import boilerplate code
 * {@code new AcrossTestWebContextBuilder()} can be reduced to {@code web()}.
 *
 * @author Arne Vandamme
 * @see AcrossTestContextBuilder
 * @see AcrossTestWebContextBuilder
 * @since feb 2016
 */
public abstract class AcrossTestBuilders
{
	/**
	 * Create a new builder for a standard (non-web) {@link com.foreach.across.core.AcrossContext}.
	 * This builder will create a {@link com.foreach.across.test.AcrossTestContext}.
	 *
	 * @return builder instance
	 */
	public static AcrossTestContextBuilder standard() {
		return new AcrossTestContextBuilder();
	}

	/**
	 * Create a new builder for a web based {@link com.foreach.across.core.AcrossContext}.
	 * This will ensure a {@link javax.servlet.ServletContext} is initialized and Spring
	 * {@link org.springframework.web.context.WebApplicationContext} is being used.
	 * This builder will create a {@link com.foreach.across.test.AcrossTestWebContext} with support for
	 * {@link org.springframework.test.web.servlet.MockMvc}.
	 *
	 * @return builder instance
	 */
	public static AcrossTestWebContextBuilder web() {
		return new AcrossTestWebContextBuilder();
	}
}
