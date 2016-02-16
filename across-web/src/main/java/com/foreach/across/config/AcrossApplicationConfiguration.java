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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Supports Spring Boot configuration of an {@link AcrossContext}, ensures bootstrap of the
 * {@link AcrossContext} is done before the webserver starts.
 *
 * @author Arne Vandamme
 * @see AcrossApplication
 */
@Import({ DispatcherServletAutoConfiguration.class,
          EmbeddedServletContainerAutoConfiguration.class,
          ServerPropertiesAutoConfiguration.class })
public class AcrossApplicationConfiguration implements ImportSelector
{
	@ConditionalOnBean(EmbeddedServletContainerFactory.class)
	@Bean
	public AcrossServletContextInitializer acrossServletContextInitializer() {
		return new AcrossServletContextInitializer();
	}

	@Override
	public String[] selectImports( AnnotationMetadata importingClassMetadata ) {
		if ( (Boolean) importingClassMetadata.getAnnotationAttributes( AcrossApplication.class.getName() )
		                                     .getOrDefault( "enableDynamicModules", true ) ) {
			return new String[] { AcrossDynamicModulesConfiguration.class.getName() };
		}
		return new String[0];
	}

	/**
	 * {@link ServletContextInitializer} that ensures that the {@link AcrossContext} is bootstrapped before the
	 * {@link ServletContext} is fully initialized.  This is required for
	 * {@link com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer} instances to work.
	 */
	public static class AcrossServletContextInitializer implements ServletContextInitializer, BeanDefinitionRegistryPostProcessor
	{
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

			AnnotationConfigEmbeddedWebApplicationContext rootContext =
					(AnnotationConfigEmbeddedWebApplicationContext) servletContext.getAttribute(
							WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE );

			// Ensure the AcrossContext has bootstrapped while the ServletContext can be modified
			AcrossContext acrossContext = rootContext.getBean( AcrossContext.class );

			servletContext.setAttribute( WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
			                             AcrossContextUtils.getApplicationContext( acrossContext ) );

		}
	}
}

