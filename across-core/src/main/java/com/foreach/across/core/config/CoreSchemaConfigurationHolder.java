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
package com.foreach.across.core.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.support.ModuleBeanSelectorUtils;
import com.foreach.across.core.database.SchemaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Optional;

/**
 * Bean that searches for the
 *
 * @author Arne Vandamme
 */
class CoreSchemaConfigurationHolder
{
	private Optional<SchemaConfiguration> schemaConfiguration = Optional.empty();

	@Autowired
	public void loadSchemaConfiguration( ConfigurableListableBeanFactory beanFactory ) {
		schemaConfiguration = ModuleBeanSelectorUtils
				.selectBeanForModule( SchemaConfiguration.class, AcrossContext.BEAN, beanFactory );
	}

	public String getDefaultSchema() {
		if ( schemaConfiguration.isPresent() ) {
			return schemaConfiguration.get().getDefaultSchema();
		}

		return null;
	}
}
