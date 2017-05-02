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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * {@link ServletContextInitializer} that ensures that the {@link AcrossContext} is bootstrapped before the
 * {@link ServletContext} is fully initialized.  This is required for
 * {@link com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer} instances to work.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class AcrossServletContextInitializer
		implements ServletContextInitializer, BeanDefinitionRegistryPostProcessor, ApplicationListener<ContextRefreshedEvent>, Ordered
{
	public static final int LISTENER_ORDER = Ordered.LOWEST_PRECEDENCE - 10;

	private final ConfigurableApplicationContext applicationContext;

	public AcrossServletContextInitializer( ConfigurableApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
		applicationContext.addApplicationListener( this );
	}

	@Override
	public int getOrder() {
		return LISTENER_ORDER;
	}

	@Override
	public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
		BeanDefinition beanDefinition = registry.getBeanDefinition( "acrossContext" );
		beanDefinition.setLazyInit( true );
	}

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
	}

	@Override
	public void onStartup( ServletContext servletContext ) throws ServletException {
		servletContext.setAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER, true );

		// Ensure the AcrossContext has bootstrapped while the ServletContext can be modified
		applicationContext.getBean( AcrossContext.class );

		servletContext.removeAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER );
	}

	@Override
	public void onApplicationEvent( ContextRefreshedEvent event ) {
		if ( event.getApplicationContext() == applicationContext ) {
			// Ensure the AcrossContext is created - even if onStartup was never called
			AcrossContext acrossContext = applicationContext.getBean( AcrossContext.class );

			// Register the root application context
			if ( applicationContext instanceof WebApplicationContext ) {
				ServletContext servletContext = ( (WebApplicationContext) applicationContext ).getServletContext();
				servletContext.setAttribute( WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				                             AcrossContextUtils.getApplicationContext( acrossContext ) );
			}
		}
	}
}
