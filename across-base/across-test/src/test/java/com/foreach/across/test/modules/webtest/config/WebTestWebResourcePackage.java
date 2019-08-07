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
package com.foreach.across.test.modules.webtest.config;

import com.foreach.across.modules.web.resource.SimpleWebResourcePackage;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Keep deprecations for compatibility testing.
 */
@SuppressWarnings("deprecation")
public class WebTestWebResourcePackage extends SimpleWebResourcePackage
{
	public static final String VERSION = "3.3.5";
	public static final String NAME = "bootstrap";

	public WebTestWebResourcePackage() {
		setWebResources(
				new WebResource( WebResource.CSS, NAME,
				                 "//maxcdn.bootstrapcdn.com/bootstrap/" + VERSION + "/css/bootstrap.min.css",
				                 WebResource.EXTERNAL ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
				                 "//maxcdn.bootstrapcdn.com/bootstrap/" + VERSION + "/js/bootstrap.min.js",
				                 WebResource.EXTERNAL )
		);
	}

	@Autowired
	public void registerPackage( WebResourcePackageManager packageManager ) {
		packageManager.register( NAME, this );
	}
}
