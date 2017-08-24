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
package com.foreach.across.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures dynamic application modules for an {@link AcrossApplication}.
 *
 * @author Arne Vandamme
 */
public class AcrossApplicationConfiguration implements ImportSelector, EnvironmentAware
{
	private ConfigurableEnvironment environment;

	private static Map<String, Object> getOrAdd( MutablePropertySources sources,
	                                             String name ) {
		if ( sources.contains( name ) ) {
			return (Map<String, Object>) sources.get( name ).getSource();
		}
		Map<String, Object> map = new HashMap<>();
		sources.addFirst( new MapPropertySource( name, map ) );
		return map;
	}

	@Override
	public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
		if ( environment.getProperty( EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY ) == null ) {
			MutablePropertySources sources = environment.getPropertySources();
			Map<String, Object> map = getOrAdd( sources, "across" );
			if ( !map.containsKey( EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY ) ) {
				map.put( EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, false );
			}
		}

		if ( (Boolean) importingClassMetadata.getAnnotationAttributes( AcrossApplication.class.getName() )
		                                     .getOrDefault( "enableDynamicModules", true ) ) {
			return new String[] { DisplayNameConfiguration.class.getName(), AcrossDynamicModulesConfiguration.class.getName() };
		}
		return new String[] { DisplayNameConfiguration.class.getName() };
	}

	@Override
	public void setEnvironment( Environment environment ) {
		Assert.isInstanceOf( ConfigurableEnvironment.class, environment );
		this.environment = (ConfigurableEnvironment) environment;
	}
}
