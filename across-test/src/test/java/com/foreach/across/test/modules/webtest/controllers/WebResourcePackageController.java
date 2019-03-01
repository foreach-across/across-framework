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
package com.foreach.across.test.modules.webtest.controllers;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.test.modules.webtest.config.WebTestWebResourcePackage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Exposed
@Controller
public class WebResourcePackageController
{
	public static final String PATH = "/webresource-package-demo";

	@RequestMapping(PATH)
	public String webResourcePage() {
		return "th/webControllers/webresource";
	}

	@ModelAttribute
	public void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( WebTestWebResourcePackage.NAME );
	}
}
