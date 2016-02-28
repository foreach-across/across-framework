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
package com.foreach.across.test.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.test.AcrossTestContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.*;

/**
 * Builder for creating an {@link AcrossTestContext}.
 * This builder allows easy configuration of properties and modules to add to an {@link AcrossTestContext}.
 * <p>
 * Once {@link #build()} has been called, the {@link AcrossTestContext} will be created and the internal
 * {@link com.foreach.across.core.AcrossContext} bootstrapped.
 *
 * @author Arne Vandamme
 * @since feb 2016
 */
public class AcrossTestContextBuilder
{
	private final List<AcrossContextConfigurer> configurers = new ArrayList<>();
	private final Properties properties = new Properties();

	public AcrossTestContextBuilder() {
		properties( properties );
	}

	/**
	 * Add one or more {@link AcrossContextConfigurer} instances to customize
	 * the {@link com.foreach.across.core.AcrossContext}.
	 *
	 * @param configurer instances
	 * @return self
	 */
	public AcrossTestContextBuilder configurer( AcrossContextConfigurer... configurer ) {
		Collections.addAll( this.configurers, configurer );
		return this;
	}

	/**
	 * Set a property value on the environment.
	 *
	 * @param key   property key
	 * @param value property value
	 * @return self
	 */
	public AcrossTestContextBuilder property( String key, Object value ) {
		properties.put( key, value );
		return this;
	}

	/**
	 * Add a properties collection to the environment.
	 *
	 * @param properties instance
	 * @return self
	 */
	public AcrossTestContextBuilder properties( Properties properties ) {
		configurers.add(
				c -> c.addPropertySources(
						new PropertiesPropertySource( "AcrossTestContextBuilder" + configurers.size(), properties )
				)
		);
		return this;
	}

	/**
	 * Add a properties map to the environment.
	 *
	 * @param properties map
	 * @return self
	 */
	public AcrossTestContextBuilder properties( Map<String, Object> properties ) {
		configurers.add(
				c -> c.addPropertySources(
						new MapPropertySource( "AcrossTestContextBuilder" + configurers.size(), properties )
				)
		);
		return this;
	}

	/**
	 * Add a {@link PropertySource} to the environment.
	 *
	 * @param propertySource instance
	 * @return self
	 */
	public AcrossTestContextBuilder properties( PropertySource propertySource ) {
		configurers.add( c -> c.addPropertySources( propertySource ) );
		return this;
	}

	/**
	 * Creates the internal {@link AcrossContext} and bootstraps it.  Then wraps it in a {@link AcrossTestContext}
	 * instance that gives easy access to querying functionality.
	 *
	 * @return queryable context containing the bootstrapped {@link AcrossContext}
	 */
	public AcrossTestContext build() {
		return new AcrossTestContext( contextConfigurers() );
	}

	protected AcrossContextConfigurer[] contextConfigurers() {
		return configurers.toArray( new AcrossContextConfigurer[configurers.size()] );
	}
}
