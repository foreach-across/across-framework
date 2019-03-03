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
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Exposed
@Controller
public class WebResourceController
{
	public static final String PATH = "/webresource-demo";

	@RequestMapping(PATH)
	public String webResourcePage() {
		return "th/webControllers/webresource";
	}

	@ModelAttribute
	public void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addWithKey( WebResource.CSS, "test-css-external", "test-css-external", WebResource.EXTERNAL );
		webResourceRegistry.addWithKey( WebResource.CSS, "test-css-views", "/test-css-views", WebResource.VIEWS );
		webResourceRegistry.addWithKey( WebResource.CSS, "test-css-inline", "test-css-inline", WebResource.INLINE );
		webResourceRegistry.addWithKey( WebResource.CSS, "test-css-data", "test-css-data", WebResource.DATA );
		webResourceRegistry.addWithKey( WebResource.CSS, "test-css-relative", "test-css-relative", WebResource.RELATIVE );

		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT, "test-javascript-inline", "test-javascript-inline", WebResource.INLINE );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT, "test-javascript-data", "test-javascript-data-value", WebResource.DATA );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT, "test-javascript-external", "test-javascript-external", WebResource.EXTERNAL );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT, "test-javascript-views", "/test-javascript-views", WebResource.VIEWS );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT, "test-javascript-relative", "test-javascript-relative", WebResource.RELATIVE );


		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "test-javascript-end-external", "test-javascript-end-external", WebResource.EXTERNAL );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "test-javascript-end-views", "/test-javascript-end-views", WebResource.VIEWS );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "test-javascript-end-relative", "test-javascript-end-relative", WebResource.RELATIVE );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "test-javascript-end-data", "test-javascript-end-data-value", WebResource.DATA );
		webResourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "test-javascript-end-inline", "test-javascript-end-inline", WebResource.INLINE );
	}
}
