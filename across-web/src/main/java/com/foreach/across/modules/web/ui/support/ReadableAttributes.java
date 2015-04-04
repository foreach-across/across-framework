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
package com.foreach.across.modules.web.ui.support;

import java.util.Map;

public interface ReadableAttributes
{
	<Y, V extends Y> V getAttribute( Class<Y> attributeType );

	<Y> Y getAttribute( String attributeName );

	<Y> Y getAttribute( String attributeName, Class<Y> attributeType );

	boolean hasAttribute( Class<?> attributeType );

	boolean hasAttribute( String attributeName );

	Map<Object, Object> getAttributes();
}
