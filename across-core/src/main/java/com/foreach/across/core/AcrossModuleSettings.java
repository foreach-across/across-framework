package com.foreach.across.core;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

/**
 * Abstract class for module settings: configure the possible settings with their (optional) default values
 * and informative description.  This class provides an easy interface to query the configured settings.
 *
 * @author Arne Vandamme
 */
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
		this.environment = environment;

		if ( environment instanceof ConfigurableEnvironment ) {
			ConfigurableEnvironment configurable = (ConfigurableEnvironment) environment;

			// Add defaults as the very last property source
			configurable.getPropertySources().addLast( registry );
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
	public <T> Class<T> getPropertyAsClass( String key, Class<T> targetType ) {
		return environment.getPropertyAsClass( key, targetType );
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
