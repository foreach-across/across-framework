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

package com.foreach.across.core;

import com.foreach.across.core.annotations.Module;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;

/**
 * Abstract class for module settings: configure the possible settings with their (optional) default values
 * and informative description.  This class provides an easy interface to query the configured settings.
 *
 * @author Arne Vandamme
 * @deprecated obsolete - favour the use of {@link org.springframework.boot.context.properties.ConfigurationProperties} - should be removed in 4.0.0
 * and Spring Boot {@link org.springframework.context.annotation.Conditional} implementations instead
 */
@Deprecated
@Module(AcrossModule.CURRENT_MODULE)
public abstract class AcrossModuleSettings implements EnvironmentAware, PropertyResolver
{
	private Environment environment;

	private final AcrossModuleSettingsRegistry registry = new AcrossModuleSettingsRegistry(
			getClass().getSimpleName() + ": default values" );

	protected AcrossModuleSettings() {
		registerSettings( registry );
	}

	public AcrossModuleSettingsRegistry getSettingsRegistry() {
		return registry;
	}

	@Override
	public void setEnvironment( Environment environment ) {
		if ( this.environment == null ) {
			this.environment = environment;

			if ( environment instanceof ConfigurableEnvironment ) {
				ConfigurableEnvironment configurable = (ConfigurableEnvironment) environment;

				// Add defaults as the very last property source
				MutablePropertySources ps = configurable.getPropertySources();
				if ( !ps.contains( registry.getName() ) ) {
					ps.addLast( registry );
				}
			}
		}
	}

	@Override
	public boolean containsProperty( String key ) {
		return environment.containsProperty( key );
	}

	@Override
	public String getProperty( String key ) {
		return registry.isRequired( key )
				? environment.getRequiredProperty( key )
				: environment.getProperty( key );
	}

	@Override
	public String getProperty( String key, String defaultValue ) {
		return environment.getProperty( key, defaultValue );
	}

	@Override
	public <T> T getProperty( String key, Class<T> targetType ) {
		return registry.isRequired( key )
				? environment.getRequiredProperty( key, targetType )
				: environment.getProperty( key, targetType );
	}

	@Override
	public <T> T getProperty( String key, Class<T> targetType, T defaultValue ) {
		return environment.getProperty( key, targetType, defaultValue );
	}

	@Override
	public String getRequiredProperty( String key ) throws IllegalStateException {
		return environment.getRequiredProperty( key );
	}

	@Override
	public <T> T getRequiredProperty( String key, Class<T> targetType ) throws IllegalStateException {
		return environment.getRequiredProperty( key, targetType );
	}

	@Override
	public String resolvePlaceholders( String text ) {
		return environment.resolvePlaceholders( text );
	}

	@Override
	public String resolveRequiredPlaceholders( String text ) throws IllegalArgumentException {
		return environment.resolveRequiredPlaceholders( text );
	}

	protected abstract void registerSettings( AcrossModuleSettingsRegistry registry );
}
