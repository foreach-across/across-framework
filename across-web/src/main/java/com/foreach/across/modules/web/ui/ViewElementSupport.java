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

public abstract class ViewElementSupport implements MutableViewElement
{
	private String name, customTemplate, elementType;

	protected ViewElementSupport( String elementType ) {
		this.elementType = elementType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MutableViewElement setName( String name ) {
		this.name = name;
		return this;
	}

	@Override
	public String getCustomTemplate() {
		return customTemplate;
	}

	@Override
	public MutableViewElement setCustomTemplate( String customTemplate ) {
		this.customTemplate = customTemplate;
		return this;
	}

	@Override
	public String getElementType() {
		return elementType;
	}

	protected MutableViewElement setElementType( String elementType ) {
		this.elementType = elementType;
		return this;
	}
}
