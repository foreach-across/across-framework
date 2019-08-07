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
package com.foreach.across.modules.web.thymeleaf;

import org.thymeleaf.context.ITemplateContext;

/**
 * Helper that generates a unique attribute name.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
final class AttributeNameGenerator
{
	static final String ATTR_NAME = AttributeNameGenerator.class.getName();

	private int counter = 0;

	/**
	 * @return a new unique attribute name
	 */
	public String generateAttributeName() {
		return "_generatedAttribute" + counter++;
	}

	/**
	 * Fetch the store from the current template context.
	 *
	 * @param context to get the store from
	 * @return id store instance
	 */
	public static AttributeNameGenerator fetch( ITemplateContext context ) {
		return (AttributeNameGenerator) context.getExpressionObjects().getObject( ATTR_NAME );
	}
}
