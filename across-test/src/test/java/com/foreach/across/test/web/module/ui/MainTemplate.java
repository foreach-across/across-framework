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

import com.foreach.across.modules.web.template.LayoutTemplateProcessorAdapterBean;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@Component
public class MainTemplate extends LayoutTemplateProcessorAdapterBean
{
	public static final String NAME = "MainTemplate";

	public MainTemplate() {
		super( NAME, "th/webControllers/templates/main" );
	}

	@Autowired
	public void registerAsDefaultTemplate( WebTemplateRegistry webTemplateRegistry ) {
		webTemplateRegistry.setDefaultTemplateName( NAME );
	}
}
