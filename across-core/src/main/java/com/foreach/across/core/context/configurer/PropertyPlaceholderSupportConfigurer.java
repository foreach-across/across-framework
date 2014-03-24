package com.foreach.across.core.context.configurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Enables property placeholder support in the application context, by creating
 * a default PropertySourcesPlaceholderConfigurer.
 *
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public class PropertyPlaceholderSupportConfigurer extends AnnotatedClassConfigurer
{
	public PropertyPlaceholderSupportConfigurer() {
		super( Config.class );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}
	}
}
