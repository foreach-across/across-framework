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
package com.foreach.across.modules.web.ui;

public interface MutableViewElement extends ViewElement
{
	MutableViewElement setName( String name );

	MutableViewElement setCustomTemplate( String customTemplate );

	/**
	 * Common {@link Wither} functions for the default {@link MutableViewElement} properties.
	 */
	interface Functions
	{
		/**
		 * Set internal {@link #getName()} property.
		 */
		static WitherSetter elementName( String name ) {
			return e -> ( (MutableViewElement) e ).setName( name );
		}

		/**
		 * Set internal {@link #getCustomTemplate()} ()} property.
		 */
		static WitherSetter customTemplate( String template ) {
			return e -> ( (MutableViewElement) e ).setCustomTemplate( template );
		}

		/**
		 * Set internal {@link #getElementType()} property.
		 */
		static WitherSetter elementType( String elementType ) {
			return e -> ( (ViewElementSupport) e ).setElementType( elementType );
		}
	}
}
