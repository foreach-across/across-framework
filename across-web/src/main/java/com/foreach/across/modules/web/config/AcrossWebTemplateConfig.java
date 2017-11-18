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

package com.foreach.across.modules.web.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.template.NamedWebTemplateProcessor;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.template.WebTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collection;

/**
 * Configures web template support with automatic registration of named web templates.
 */
@Configuration
@ConditionalOnProperty(value = "acrossWebModule.templates.enabled", matchIfMissing = true)
@OrderInModule(3)
public class AcrossWebTemplateConfig extends WebMvcConfigurerAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebTemplateConfig.class );

	@Autowired
	private AcrossWebModuleSettings settings;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( webTemplateInterceptor() );
	}

	@Bean
	@Exposed
	public WebTemplateRegistry webTemplateRegistry() {
		return new WebTemplateRegistry();
	}

	@Bean
	public WebTemplateInterceptor webTemplateInterceptor() {
		return new WebTemplateInterceptor( webTemplateRegistry() );
	}

	@PostRefresh
	public void registerNamedWebTemplateProcessors() {
		if ( settings.getTemplates().isAutoRegister() ) {
			LOG.info( "Scanning modules for NamedWebTemplateProcessor instances" );

			Collection<NamedWebTemplateProcessor> namedProcessors = beanRegistry.getBeansOfType(
					NamedWebTemplateProcessor.class,
					true );
			WebTemplateRegistry registry = webTemplateRegistry();

			for ( NamedWebTemplateProcessor webTemplateProcessor : namedProcessors ) {
				registry.register( webTemplateProcessor );
			}
		}
	}
}
