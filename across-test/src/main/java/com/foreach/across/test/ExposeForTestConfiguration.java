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
package com.foreach.across.test;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * Adjusts the context configuration based on specific attribute values on {@link AcrossTestConfiguration}.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Configuration
class ExposeForTestConfiguration implements ImportAware, AcrossBootstrapConfigurer
{
	private Class<?>[] types = new Class[0];

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		Map<String, Object> configuration = importMetadata.getAnnotationAttributes( ExposeForTest.class.getName() );
		types = (Class<?>[]) configuration.get( "value" );
	}

	@Override
	public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
		moduleConfiguration.expose( types );
	}
}
