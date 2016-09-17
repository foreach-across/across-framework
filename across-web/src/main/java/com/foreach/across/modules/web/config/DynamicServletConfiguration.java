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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import com.foreach.across.modules.web.servlet.ServletContextInitializerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.List;

/**
 * Responsible for executing the {@link AcrossWebDynamicServletConfigurer} instances
 * found throughout the {@link AcrossContext}.
 *
 * @author Arne Vandamme
 */
@Configuration
public class DynamicServletConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( DynamicServletConfiguration.class );

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Autowired
	private ServletContext servletContext;

	@Bean
	public ServletContextInitializerConfigurer servletContextInitializerConfigurer( AcrossContextInfo contextInfo ) {
		return new ServletContextInitializerConfigurer( contextInfo );
	}

	@Event
	public void registerServletsAndFilters( AcrossContextBootstrappedEvent bootstrappedEvent ) {
		List<AcrossWebDynamicServletConfigurer> configurers
				= beanRegistry.getBeansOfType( AcrossWebDynamicServletConfigurer.class, true );

		Object loader = servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER );
		boolean dynamicConfigurationAllowed = loader != null;

		try {
			for ( AcrossWebDynamicServletConfigurer configurer : configurers ) {
				LOG.debug( "Registering dynamic servlets from bean {}", configurer.getClass() );
				configurer.configure( servletContext, dynamicConfigurationAllowed );
			}
		}
		catch ( ServletException se ) {
			throw new AcrossException( se );
		}
	}
}
