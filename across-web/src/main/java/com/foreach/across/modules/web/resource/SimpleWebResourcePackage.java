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

import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Groups a number of WebResources together.
 */
public class SimpleWebResourcePackage implements WebResourcePackage
{
	private Collection<WebResource> webResources = Collections.emptyList();
	private String[] dependencies = new String[0];

	protected SimpleWebResourcePackage() {
	}

	public SimpleWebResourcePackage( Collection<WebResource> webResources ) {
		setWebResources( webResources );
	}

	protected void setWebResources( WebResource... webResources ) {
		setWebResources( Arrays.asList( webResources ) );
	}

	protected void setWebResources( @NonNull Collection<WebResource> webResources ) {
		Assert.notEmpty( webResources, "webResources must not be empty" );
		this.webResources = webResources;
	}

	protected void setDependencies( Collection<String> packageNames ) {
		setDependencies( packageNames.toArray( new String[0] ) );
	}

	protected void setDependencies( String... packageNames ) {
		this.dependencies = packageNames;
	}

	@Override
	public void install( WebResourceRegistry registry ) {
		registry.addPackage( dependencies );

		for ( WebResource webResource : webResources ) {
			registry.add( webResource );
		}
	}

	@Override
	public void uninstall( WebResourceRegistry registry ) {
		for ( WebResource webResource : webResources ) {
			registry.removeResource( webResource.getType(), webResource.getData() );
		}
	}
}
