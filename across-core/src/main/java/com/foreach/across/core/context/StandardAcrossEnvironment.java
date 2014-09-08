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

package com.foreach.across.core.context;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 * Merge semantics for an Across environment are different: the standard property sources
 * are kept in the order of the parent environment.
 *
 * @author Arne Vandamme
 */
public class StandardAcrossEnvironment extends StandardEnvironment
{
	@Override
	public void merge( ConfigurableEnvironment parent ) {
		removeIfParentContains( parent, SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME );
		removeIfParentContains( parent, SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME );

		super.merge( parent );
	}

	private void removeIfParentContains( ConfigurableEnvironment parent, String sourceName ) {
		if ( parent.getPropertySources().contains( sourceName ) ) {
			getPropertySources().remove( sourceName );
		}
	}
}
