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

import java.util.HashMap;
import java.util.Map;

/**
 * Allows a collection of WebResource instances to be bundled under a single name.
 */
public class WebResourcePackageManager
{
	private final Map<String, WebResourcePackage> packages = new HashMap<>();

	public void register( String name, WebResourcePackage webResourcePackage ) {
		packages.put( name, webResourcePackage );
	}

	public void unregister( String name ) {
		packages.remove( name );
	}

	public WebResourcePackage getPackage( String name ) {
		return packages.get( name );
	}
}
