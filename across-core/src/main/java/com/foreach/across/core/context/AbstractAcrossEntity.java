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
	private AcrossApplicationContextHolder acrossApplicationContextHolder;

	private Properties properties = new Properties();

	public AcrossApplicationContextHolder getAcrossApplicationContextHolder() {
		return acrossApplicationContextHolder;
	}

	public void setAcrossApplicationContextHolder( AcrossApplicationContextHolder acrossApplicationContextHolder ) {
		this.acrossApplicationContextHolder = acrossApplicationContextHolder;
	}

	public boolean hasApplicationContext() {
		return acrossApplicationContextHolder != null;
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
