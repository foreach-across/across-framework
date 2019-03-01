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
package com.foreach.across.modules.web.events;

import com.foreach.across.core.events.NamedAcrossEvent;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;

/**
 * Event published by a {@link com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean} after
 * a named template has been prepared.
 *
 * @author Arne Vandamme
 */
public class BuildTemplateWebResourcesEvent implements NamedAcrossEvent
{
	private final String templateName;
	private final WebResourceRegistry webResourceRegistry;

	public BuildTemplateWebResourcesEvent( String templateName, WebResourceRegistry webResourceRegistry ) {
		this.templateName = templateName;
		this.webResourceRegistry = webResourceRegistry;
	}

	@Override
	public String getEventName() {
		return getTemplateName();
	}

	public String getTemplateName() {
		return templateName;
	}

	public WebResourceRegistry getWebResourceRegistry() {
		return webResourceRegistry;
	}

	public void add( WebResource webResource ) {
		webResourceRegistry.add( webResource );
	}

	/**
	 * Register a new resource with the default location.
	 * Since there is no key, any other resource of the same type with the same data will be replaced.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Data to register.
	 */
	public void add( String type, Object data ) {
		webResourceRegistry.add( type, data );
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
		webResourceRegistry.addWithKey( type, key, data );
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
		webResourceRegistry.addWithKey( type, key, data, location );
	}

	/**
	 * Will remove all registered resources with the given content.
	 * Requires that the resource data equals() the requested data.
	 *
	 * @param data Content the resource should have.
	 */
	public void removeResource( Object data ) {
		webResourceRegistry.removeResource( data );
	}

	/**
	 * Will remove all registered resources of that type with the given content.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param data Content the resource should have.
	 */
	public void removeResource( String type, Object data ) {
		webResourceRegistry.removeResource( type, data );
	}

	/**
	 * Will remove all resources registered under the key specified.
	 *
	 * @param key Key the resource is registered under.
	 */
	public void removeResourceWithKey( String key ) {
		webResourceRegistry.removeResourceWithKey( key );
	}

	/**
	 * Will remove all resources of that type registered under the key specified.
	 *
	 * @param type Type of the resource, see {@link com.foreach.across.modules.web.resource.WebResource} for constants.
	 * @param key  Key the resource is registered under.
	 */
	public void removeResourceWithKey( String type, String key ) {
		webResourceRegistry.removeResourceWithKeyFromBucket( type, key );
	}

	/**
	 * Installs all resources attached to the packages with the names specified.
	 * This requires the packages to be registered in the attached WebResourcePackageManager.
	 *
	 * @param packageNames Names of the packages to install.
	 */
	public void addPackage( String... packageNames ) {
		webResourceRegistry.addPackage( packageNames );
	}

	/**
	 * Will remove all resources of the packages with the specified names.
	 *
	 * @param packageNames Names of the packages.
	 */
	public void removePackage( String... packageNames ) {
		webResourceRegistry.removePackage( packageNames );
	}
}
