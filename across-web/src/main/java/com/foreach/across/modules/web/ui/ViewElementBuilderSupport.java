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

public abstract class ViewElementBuilderSupport<T extends ViewElement, SELF extends ViewElementBuilder>
		implements ViewElementBuilder<T>
{
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
