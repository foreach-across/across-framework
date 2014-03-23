package com.foreach.across.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Enables property placeholder support in the application context, by creating
 * a default PropertySourcesPlaceholderConfigurer.
 *
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
@Configuration
public class PropertyPlaceholderSupportConfiguration
{
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
