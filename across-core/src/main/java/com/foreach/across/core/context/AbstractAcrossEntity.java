package com.foreach.across.core.context;

import com.foreach.across.core.AcrossException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractAcrossEntity implements AcrossEntity
{
	private AcrossApplicationContext acrossApplicationContext;

	private Properties properties = new Properties();

	public AcrossApplicationContext getAcrossApplicationContext() {
		return acrossApplicationContext;
	}

	public void setAcrossApplicationContext( AcrossApplicationContext acrossApplicationContext ) {
		this.acrossApplicationContext = acrossApplicationContext;
	}

	public boolean hasApplicationContext() {
		return acrossApplicationContext != null;
	}

	/**
	 * Get the properties set on the Across entity.  These properties
	 * will be available when the entity is being bootstrapped.
	 *
	 * @return Properties set directly on the Across entity.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Set the property collection on the Across entity.
	 * These should be available during bootstrap of the entity.
	 *
	 * @param properties Properties to make available during bootstrap.
	 */
	public void setProperties( Properties properties ) {
		this.properties = properties;
	}

	/**
	 * Set a single property value that should be available during bootstrap.
	 *
	 * @param name  Property name.
	 * @param value Property value.
	 */
	public void setProperty( String name, Object value ) {
		this.properties.put( name, value );
	}

	/**
	 * Add PropertySources to the context.
	 *
	 * @param propertySources A PropertySources instance.
	 */
	public abstract void addPropertySources( PropertySources propertySources );

	/**
	 * Add PropertySources to the context.
	 *
	 * @param propertySources One or more PropertySource instances.
	 */
	public abstract void addPropertySources( PropertySource<?>... propertySources );

	/**
	 * <p>Adds one or more Resources instances for properties loading to the context.  These
	 * will be registered as PropertySource.</p>
	 * <p>If a resource does not exist, it will be silently ignored.</p>
	 *
	 * @param resources Resources to add.
	 * @see org.springframework.core.env.PropertySource
	 */
	public void addPropertySources( Resource... resources ) {
		MutablePropertySources propertySources = new MutablePropertySources();
		for ( Resource resource : resources ) {
			if ( resource.exists() ) {
				try {
					propertySources.addLast( new ResourcePropertySource( resource ) );
				}
				catch ( IOException ioe ) {
					throw new AcrossException( "Failed to load property resource " + resource, ioe );
				}
			}
		}

		addPropertySources( propertySources );
	}
}
