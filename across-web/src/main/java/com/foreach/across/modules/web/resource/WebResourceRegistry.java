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

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.foreach.across.modules.web.resource.WebResource.*;

/**
 * <p>Registry for a set of web resources.  Usually there is one registry per view.
 * Used to specify css files, javascript files to be loaded from code, including a rendering order.</p>
 * <p>A resource is configured with a certain type and a location.  These will determine how and when
 * the resource will be rendered.</p>
 * <p><strong>When adding resources, both key and content are unique discriminators.  The same content can only
 * be added once, unless a different key is explicitly provided.</strong></p>
 * <p>Loosely based on Drupal constructions.</p>
 * To declare a set of {@link WebResourceReference} items you can use the following construct:
 *
 * <pre>{@code
 * webResourceRegistry.apply(
 *     WebResourceRule.add( WebResource.css( "@static:/css/bootstrap.min.css" ) ).withKey( "bootstrap-min-css" ).toBucket( CSS ),
 *     WebResourceRule.add( WebResource.javascript( "bootstrap.min.js" ) ).withKey( "bootstrap-min-js" ).toBucket( JAVASCRIPT_PAGE_END ),
 *     WebResourceRule.add( WebResource.css().inline( "body {background-color: powderblue;}" ) ).withKey( "inline-body-blue" ).toBucket( CSS )
 * );
 * }</pre>
 */
public class WebResourceRegistry
{
	private String defaultLocation = WebResource.RELATIVE;

	private final WebResourcePackageManager packageManager;
	private final Map<String, List<WebResourceReference>> webResources = new LinkedHashMap<>();

	private final Set<String> installedPackages = new HashSet<>();

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
	 * @deprecated since 3.2.0 - replaced by {@link WebResourceRule#add(ViewElementBuilder)}
	 */
	@Deprecated
	public void add( @NonNull WebResource webResource ) {
		addWithKey( webResource.getType(), webResource.getKey(), webResource.getData(), webResource.getLocation() );
	}

	/**
	 * Register a new resource with the default location.
	 * Since there is no key, any other resource of the same type with the same data will be replaced.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Data to register.
	 * @deprecated since 3.2.0 - replaced by {@link WebResourceRule#add(ViewElementBuilder)}
	 */
	@Deprecated
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
	 * @deprecated since 3.2.0 - replaced by {@link WebResourceRule#add(ViewElementBuilder)}
	 */
	@Deprecated
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
	 * @deprecated since 3.2.0 - replaced by {@link WebResourceRule#add(ViewElementBuilder)}
	 */
	@Deprecated
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
	 * @deprecated since 3.2.0 - replaced by {@link WebResourceRule#add(ViewElementBuilder)}
	 */
	@Deprecated
	public void addWithKey( String type, String key, Object data, String location ) {
		WebResource existing = findResource( type, key, data );

		if ( existing == null ) {
			List<WebResourceReference> rules = webResources.computeIfAbsent( type, k -> new LinkedList<>() );
			AddWebResourceRule rule;
			switch ( type ) {
				case JAVASCRIPT:
				case JAVASCRIPT_PAGE_END:
					if ( data instanceof String ) {
						rule = WebResourceRule.add( WebResource.javascript( (String) data ) ).withKey( key ).toBucket( type );
					}
					else {
						rule = WebResourceRule.add( WebResource.javascript().data( data ) ).withKey( key ).toBucket( type );
					}

					break;
				case CSS:
					rule = WebResourceRule.add( WebResource.javascript( (String) data ) ).withKey( key ).toBucket( type );
					break;
				default:
					rule = new AddWebResourceRule().withKey( key ).toBucket( type );
			}

			WebResource resource = new WebResource( type, key, data, location );
			AddWebResourceRule addWebResourceRule = rule;
			rules.add( new WebResourceReference( addWebResourceRule.getViewElementBuilder(), key, addWebResourceRule.getBefore(),
			                                     addWebResourceRule.getAfter(), addWebResourceRule.getOrder(), resource ) );
		}
		else {
			existing.setKey( key );
			existing.setData( data );
			existing.setLocation( location );
		}
	}

	private WebResource findResource( String type, String key, Object data ) {
		WebResource matchOnKey = null, matchOnData = null;

		List<WebResourceReference> references = webResources.get( type );
		if ( references != null ) {
			for ( WebResourceReference reference : references ) {
				WebResource resource = reference.getResource();
				if ( resource != null ) {
					// We are interested in resources with the same key
					if ( key != null && StringUtils.equals( key, resource.getKey() ) ) {
						matchOnKey = resource;
					}

					// A resource without key but the same data will always match
					if ( !resource.hasKey() && Objects.equals( data, resource.getData() ) ) {
						matchOnData = resource;
					}
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
	 * @deprecated since 3.2.0 - removing by "data" is not supported anymore
	 */
	@Deprecated
	public void removeResource( Object data ) {
		for ( Map.Entry<String, List<WebResourceReference>> references : webResources.entrySet() ) {
			// Only for old style references
			for ( WebResourceReference reference : references.getValue() ) {
				WebResource resource = reference.getResource();
				if ( resource != null ) {
					if ( Objects.equals( data, resource.getData() ) ) {
						references.getValue().remove( reference );
					}
				}
			}
		}
	}

	/**
	 * Will remove all registered resources of that type with the given content.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Content the resource should have.
	 * @deprecated since 3.2.0 - removing by "data" is not supported anymore
	 */
	@Deprecated
	public void removeResource( String type, Object data ) {
		List<WebResourceReference> references = webResources.get( type );
		if ( references != null ) {
			// Only for old style references
			for ( WebResourceReference reference : references ) {
				WebResource resource = reference.getResource();
				if ( resource != null ) {
					if ( Objects.equals( data, resource.getData() ) ) {
						references.remove( reference );
					}
				}
			}
		}
	}

	/**
	 * Will remove all resources registered under the key specified.
	 *
	 * @param key Key the resource is registered under.
	 */
	public void removeResourceWithKey( @NonNull String key ) {
		for ( List<WebResourceReference> resources : webResources.values() ) {
			resources.removeIf( resource -> StringUtils.equals( key, resource.getKey() ) );
		}
	}

	/**
	 * Will remove all resources of that type registered under the key specified.
	 *
	 * @param bucket Bucket name, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param key    Key the resource is registered under.
	 */
	public void removeResourceWithKey( @NonNull String bucket, @NonNull String key ) {
		List<WebResourceReference> references = webResources.get( bucket );
		if ( references != null ) {
			references.removeIf( resource -> StringUtils.equals( key, resource.getKey() ) );
		}
	}

	/**
	 * Installs all resources attached to the packages with the names specified.
	 * This requires the packages to be registered in the attached WebResourcePackageManager.
	 *
	 * @param packageNames Names of the packages to install.
	 */
	public void addPackage( @NonNull String... packageNames ) {
		for ( String packageName : packageNames ) {
			if ( !installedPackages.contains( packageName ) ) {
				WebResourcePackage webResourcePackage = packageManager.getPackage( packageName );

				if ( webResourcePackage == null ) {
					throw new IllegalArgumentException( "No WebResourcePackage found with name " + packageName );
				}

				installedPackages.add( packageName );
				webResourcePackage.install( this );
			}
		}
	}

	/**
	 * Will remove all resources of the packages with the specified names.
	 *
	 * @param packageNames Names of the packages.
	 */
	public void removePackage( @NonNull String... packageNames ) {
		for ( String packageName : packageNames ) {
			WebResourcePackage webResourcePackage = packageManager.getPackage( packageName );

			// Package not found is ignored
			if ( webResourcePackage != null ) {
				webResourcePackage.uninstall( this );
				installedPackages.remove( packageName );
			}
		}
	}

	/**
	 * Clears the entire registry, for all buckets.
	 */
	public void clear() {
		webResources.values().clear();
	}

	/**
	 * Removes all resources in the given bucket.
	 *
	 * @param bucket Name of the bucket.
	 */
	public void clear( @NonNull String bucket ) {
		List<WebResourceReference> references = webResources.get( bucket );
		if ( references != null ) {
			references.clear();
		}
	}

	/**
	 * Lists all resources for a given type.
	 *
	 * @param type Type of the resource.
	 * @return Collection of WebResource instances.
	 * @deprecated since 3.2.0 - replaced by {@link #getBucketResources(String)}
	 */
	@Deprecated
	public Collection<WebResource> getResources( String type ) {
		List<WebResource> filtered = new LinkedList<>();

		List<WebResourceReference> resources = webResources.get( type );
		if ( resources != null ) {
			for ( WebResourceReference resource : resources ) {
				WebResource webResource = resource.getResource();
				if ( webResource != null ) {
					filtered.add( webResource );
				}
			}
		}

		return filtered;
	}

	/**
	 * Lists all resources in this registry.
	 *
	 * @return Collection of WebResource instances.
	 * @deprecated since 3.2.0 - replaced by {@link #getBucketResources(String)}
	 */
	@Deprecated
	public Collection<WebResource> getResources() {
		List<WebResource> items = new LinkedList<>();
		for ( Map.Entry<String, List<WebResourceReference>> webResources : webResources.entrySet() ) {
			for ( WebResourceReference reference : webResources.getValue() ) {
				if ( reference.getResource() != null ) {
					items.add( reference.getResource() );
				}
			}
		}
		return items;
	}

	/**
	 * Return all bucket names in this registry.
	 */
	public Set<String> getBuckets() {
		return Collections.unmodifiableSet( webResources.keySet() );
	}

	/**
	 * Return a {@link WebResourceReferenceCollection} from all resources in this registry for a specific bucket.
	 *
	 * @param bucket The bucket name.
	 */
	public WebResourceReferenceCollection getBucketResources( @NonNull String bucket ) {
		List<WebResourceReference> filtered = new LinkedList<>();

		List<WebResourceReference> items = webResources.get( bucket );
		if ( items != null ) {
			filtered.addAll( items );
		}

		return new WebResourceReferenceCollection( filtered );
	}

	/**
	 * Merges all resources of the other registry in this one.
	 *
	 * @param registry Registry containing resource to be copied.
	 */
	public void merge( WebResourceRegistry registry ) {
		if ( registry != null ) {
			for ( Map.Entry<String, List<WebResourceReference>> references : registry.webResources.entrySet() ) {
				for ( WebResourceReference reference : references.getValue() ) {
					add( references.getKey(), reference );
				}
			}
		}
	}

	/**
	 * Will apply the set of {@link com.foreach.across.modules.web.resource.WebResourceRule} items to the  registry
	 */
	public void apply( @NonNull WebResourceRule... webResourceRules ) {
		for ( WebResourceRule webResourceRule : webResourceRules ) {
			webResourceRule.applyTo( this );
		}
	}

	/**
	 * Adds a specific {@link WebResourceReference} to the specified bucket, creating the bucket if it does not exist.
	 */
	public void add( @NonNull String bucket, @NonNull WebResourceReference webResourceReference ) {
		this.webResources.computeIfAbsent( bucket, w -> new LinkedList<>() ).add( webResourceReference );
	}
}
