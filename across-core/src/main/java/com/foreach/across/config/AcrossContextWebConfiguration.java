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
package com.foreach.across.config;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;
import com.foreach.across.core.context.web.WebBootstrapApplicationContextFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Custom configuration that modifies the Across context bootstrapper in case
 * of a web application. It ensures that {@link com.foreach.across.core.context.web.AcrossWebApplicationContext} instances
 * will be used for Across context and modules.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Configuration
@ConditionalOnWebApplication
public class AcrossContextWebConfiguration implements BootstrapAdapter
{
	@Override
	public void customizeBootstrapper( AcrossBootstrapper bootstrapper ) {
		bootstrapper.setApplicationContextFactory( new WebBootstrapApplicationContextFactory() );
	}
}
