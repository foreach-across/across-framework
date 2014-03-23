package com.foreach.across.core.context.configurer;

import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

public class PropertySourcesConfigurer extends ApplicationContextConfigurerAdapter
{
	private final MutablePropertySources propertySources;

	public PropertySourcesConfigurer( PropertySources propertySources ) {
		this.propertySources = new MutablePropertySources( propertySources );
	}

	public PropertySourcesConfigurer( PropertySource<?>... propertySources ) {
		this.propertySources = new MutablePropertySources();

		for ( PropertySource<?> propertySource : propertySources ) {
			this.propertySources.addLast( propertySource );
		}
	}

	/**
	 * Returns a PropertySources instance with configured property sources to make available.
	 *
	 * @return PropertySources instance or null.
	 */
	@Override
	public PropertySources propertySources() {
		return propertySources;
	}
}
