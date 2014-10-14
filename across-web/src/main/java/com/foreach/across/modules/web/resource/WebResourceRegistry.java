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

package com.foreach.across.modules.web.resource;

import com.foreach.across.core.AcrossException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * <p>Registry for a set of web resources.  Usually there is one registry per view.
 * Used to specify css files, javascript files to be loaded from code, including a rendering order.</p>
 * <p>A resource is configured with a certain type and a location.  These will determine how and when
 * the resource will be rendered.</p>
 * <p><strong>When adding resources, both key and content are unique discriminators.  The same content can only
 * be added once, unless a different key is explicitly provided.</strong></p>
 * <p>Loosely based on Drupal constructions.</p>
 */
public class WebResourceRegistry
{
	private String defaultLocation = WebResource.RELATIVE;

	private final WebResourcePackageManager packageManager;
	private final List<WebResource> resources = new LinkedList<>();

	public WebResourceRegistry( WebResourcePackageManager packageManager ) {
		this.packageManager = packageManager;
	}

	/**
	 * @return The default location resources will be registered with.
	 * @see com.foreach.across.modules.web.resource.WebResource
	 */
	public String getDefaultLocation() {
		return defaultLocation;
	}

	/**
	 * @param defaultLocation Default location to set.
	 * @see com.foreach.across.modules.web.resource.WebResource
	 */
	public void setDefaultLocation( String defaultLocation ) {
		this.defaultLocation = defaultLocation;
	}

	/**
	 * Register a specific resource.
	 *
	 * @param webResource WebResource instance to add.
	 */
	public void add( WebResource webResource ) {
		Assert.notNull( webResource );
		addWithKey( webResource.getType(), webResource.getKey(), webResource.getData(), webResource.getLocation() );
	}

	/**
	 * Register a new resource with the default location.
	 * Since there is no key, any other resource of the same type with the same data will be replaced.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Data to register.
	 */
	public void add( String type, Object data ) {
		add( type, data, getDefaultLocation() );
	}

	/**
	 * Registers a resource with the location specified.
	 * Since there is no key, any other resource of the same type with the same data will be replaced.
	 *
	 * @param type     Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data     Data to register.
	 * @param location Where the data is available.
	 */
	public void add( String type, Object data, String location ) {
		addWithKey( type, null, data, location );
	}

	/**
	 * Registers a resource under the given key.  For complex interactions, it is often better to provide a key.
	 * Existing resources of this type with the same key will be replaced.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param key  Unique key under which to register a resource.
	 * @param data Data to register.
	 */
	public void addWithKey( String type, String key, Object data ) {
		addWithKey( type, key, data, getDefaultLocation() );
	}

	/**
	 * Registers a resource under the given key.  For complex interactions, it is often better to provide a key.
	 * Existing resources of this type with the same key will be replaced.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param key  Unique key under which to register a resource.
	 * @param data Data to register.
	 */
	public void addWithKey( String type, String key, Object data, String location ) {
		WebResource existing = findResource( type, key, data );

		if ( existing == null ) {
			resources.add( new WebResource( type, key, data, location ) );
		}
		else {
			existing.setKey( key );
			existing.setData( data );
			existing.setLocation( location );
		}
	}

	private WebResource findResource( String type, String key, Object data ) {
		WebResource matchOnKey = null, matchOnData = null;

		for ( WebResource resource : resources ) {
			if ( StringUtils.equals( type, resource.getType() ) ) {
				// We are interested in resources with the same key
				if ( key != null && StringUtils.equals( key, resource.getKey() ) ) {
					matchOnKey = resource;
				}

				// A resource without key but the same data will always match
				if ( !resource.hasKey() && ObjectUtils.equals( data, resource.getData() ) ) {
					matchOnData = resource;
				}
			}
		}

		return matchOnKey != null ? matchOnKey : matchOnData;
	}

	/**
	 * Will remove all registered resources with the given content.
	 * Requires that the resource data equals() the requested data.
	 *
	 * @param data Content the resource should have.
	 */
	public void removeResource( Object data ) {
		Iterator<WebResource> iterator = resources.iterator();

		while ( iterator.hasNext() ) {
			if ( ObjectUtils.equals( data, iterator.next().getData() ) ) {
				iterator.remove();
			}
		}
	}

	/**
	 * Will remove all registered resources of that type with the given content.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Content the resource should have.
	 */
	public void removeResource( String type, Object data ) {
		Iterator<WebResource> iterator = resources.iterator();

		while ( iterator.hasNext() ) {
			WebResource resource = iterator.next();
			if ( StringUtils.equals( type, resource.getType() ) && ObjectUtils.equals( data, resource.getData() ) ) {
				iterator.remove();
			}
		}
	}

	/**
	 * Will remove all resources registered under the key specified.
	 *
	 * @param key Key the resource is registered under.
	 */
	public void removeResourceWithKey( String key ) {
		Iterator<WebResource> iterator = resources.iterator();

		while ( iterator.hasNext() ) {
			if ( StringUtils.equals( key, iterator.next().getKey() ) ) {
				iterator.remove();
			}
		}
	}

	/**
	 * Will remove all resources of that type registered under the key specified.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param key  Key the resource is registered under.
	 */
	public void removeResourceWithKey( String type, String key ) {
		Iterator<WebResource> iterator = resources.iterator();

		while ( iterator.hasNext() ) {
			WebResource resource = iterator.next();
			if ( StringUtils.equals( type, resource.getType() ) && StringUtils.equals( key, resource.getKey() ) ) {
				iterator.remove();
			}
		}
	}

	/**
	 * Installs all resources attached to the packages with the names specified.
	 * This requires the packages to be registered in the attached WebResourcePackageManager.
	 *
	 * @param packageNames Names of the packages to install.
	 */
	public void addPackage( String... packageNames ) {
		for ( String packageName : packageNames ) {
			WebResourcePackage webResourcePackage = packageManager.getPackage( packageName );

			if ( webResourcePackage == null ) {
				throw new AcrossException( "No WebResourcePackage found with name " + packageName );
			}

			webResourcePackage.install( this );
		}
	}

	/**
	 * Will remove all resources of the packages with the specified names.
	 *
	 * @param packageNames Names of the packages.
	 */
	public void removePackage( String... packageNames ) {
		for ( String packageName : packageNames ) {
			WebResourcePackage webResourcePackage = packageManager.getPackage( packageName );

			// Package not found is ignored
			if ( webResourcePackage != null ) {
				webResourcePackage.uninstall( this );
			}
		}
	}

	/**
	 * Clears the entire registry.
	 */
	public void clear() {
		resources.clear();
	}

	/**
	 * Removes all resources of the given type.
	 *
	 * @param type Type of the resource.
	 */
	public void clear( String type ) {
		Iterator<WebResource> iterator = resources.iterator();

		while ( iterator.hasNext() ) {
			if ( StringUtils.equals( type, iterator.next().getType() ) ) {
				iterator.remove();
			}
		}
	}

	/**
	 * Lists all resources for a given type.
	 *
	 * @param type Type of the resource.
	 * @return Collection of WebResource instances.
	 */
	public Collection<WebResource> getResources( String type ) {
		List<WebResource> filtered = new LinkedList<WebResource>();

		for ( WebResource resource : resources ) {
			if ( StringUtils.equals( type, resource.getType() ) ) {
				filtered.add( resource );
			}
		}

		return filtered;
	}

	/**
	 * Lists all resources in this registry.
	 *
	 * @return Collection of WebResource instances.
	 */
	public Collection<WebResource> getResources() {
		return new ArrayList<WebResource>( resources );
	}

	/**
	 * Merges all resources of the other registry in this one.
	 *
	 * @param registry Registry containing resource to be copied.
	 */
	public void merge( WebResourceRegistry registry ) {
		if ( registry != null ) {
			for ( WebResource other : registry.resources ) {
				addWithKey( other.getType(), other.getKey(), other.getData(), other.getLocation() );
			}
		}
	}
}
