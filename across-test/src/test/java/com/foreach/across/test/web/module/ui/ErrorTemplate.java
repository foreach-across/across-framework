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
package com.foreach.across.test.web.module.ui;

import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceRule;
import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@Component
public class ErrorTemplate extends LayoutTemplateProcessorAdapterBean
{
	public static final String NAME = "ErrorTemplate";

	public ErrorTemplate() {
		super( NAME, "th/webControllers/templates/error" );
	}

	@Override
	protected void registerWebResources( WebResourceRegistry registry ) {
		registry.apply( WebResourceRule.add( WebResource.css( "@resource:/error.css" ) ).toBucket( WebResource.CSS ) );
	}
}
