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

import org.springframework.core.env.EnumerablePropertySource;

import java.util.*;

/**
 * @author Arne Vandamme
 * @deprecated since 1.1.2 - should be removed in 4.0.0
 */
public class AcrossModuleSettingsRegistry extends EnumerablePropertySource<Map<String, Object>>
{
	private final Map<String, PropertyInfo> settings = new HashMap<>();

	public AcrossModuleSettingsRegistry( String name ) {
		super( name, new HashMap<String, Object>() );
	}

	/**
	 * Registers a single optional property with default value.
	 */
	public <T> void register( String property, Class<T> propertyType, T defaultValue ) {
		register( property, propertyType, defaultValue, false, null );
	}

	public <T> void register( String property, Class<T> propertyType, T defaultValue, String description ) {
		register( property, propertyType, defaultValue, false, description );
	}

	/**
	 * Registers a single required property without a default value.
	 */
	public <T> void require( String property, Class<T> propertyType ) {
		register( property, propertyType, null, true, null );
	}

	public <T> void require( String property, Class<T> propertyType, String description ) {
		register( property, propertyType, null, true, description );
	}

	private <T> void register( String property,
	                           Class<T> propertyType,
	                           T defaultValue,
	                           boolean required,
	                           String description ) {
		if ( defaultValue != null ) {
			source.put( property, defaultValue );
		}

		settings.put( property, new PropertyInfo( property, description, propertyType, defaultValue, required ) );
	}

	public Collection<PropertyInfo> getConfigurableSettings() {
		return settings.values();
	}

	public Collection<PropertyInfo> getRequiredSettings() {
		List<PropertyInfo> required = new LinkedList<>();

		for ( PropertyInfo info : getConfigurableSettings() ) {
			if ( info.isRequired() ) {
				required.add( info );
			}
		}

		return required;
	}

	public boolean hasRequiredSettings() {
		return !getRequiredSettings().isEmpty();
	}

	@Override
	public Object getProperty( String name ) {
		return this.source.get( name );
	}

	@Override
	public String[] getPropertyNames() {
		return this.source.keySet().toArray( new String[source.size()] );
	}

	public boolean isRequired( String key ) {
		PropertyInfo info = settings.get( key );

		return info != null && info.isRequired();
	}

	public static class PropertyInfo
	{
		private final String name;
		private final String description;
		private final Class propertyType;
		private final Object defaultValue;
		private final boolean required;

		public PropertyInfo( String name,
		                     String description,
		                     Class propertyType,
		                     Object defaultValue,
		                     boolean required ) {
			this.name = name;
			this.description = description;
			this.propertyType = propertyType;
			this.defaultValue = defaultValue;
			this.required = required;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Class getPropertyType() {
			return propertyType;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public boolean isRequired() {
			return required;
		}
	}
}
