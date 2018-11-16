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
package com.foreach.across.autoconfigure;

import com.foreach.across.boot.ConditionalOnAutoConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Auto-configuration when AcrossWebModule is on the class path.
 * Configures default error pages in case of an embedded servlet container.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
@ConditionalOnBean(ErrorPageRegistry.class)
@ConditionalOnAutoConfiguration(ErrorMvcAutoConfiguration.class)
@Import(AcrossWebModuleAutoConfiguration.MyBeans.class)
public class AcrossWebModuleAutoConfiguration
{
	private final ServerProperties serverProperties;
	private final DispatcherServletPath dispatcherServletPath;

	static class MyBeans implements ImportBeanDefinitionRegistrar
	{
		@Override
		public void registerBeanDefinitions( AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry ) {
			registry.removeBeanDefinition( "errorPageCustomizer" );
			registry.registerAlias( "acrossErrorPageCustomizer", "errorPageCustomizer" );
		}
	}
	/**
	 * Configure the error pages.
	 */
	@Bean
	public ErrorPageCustomizer acrossErrorPageCustomizer() {
		return new ErrorPageCustomizer( this.serverProperties, this.dispatcherServletPath );
	}

	/**
	 * {@link org.springframework.boot.web.server.WebServerFactoryCustomizer} that configures the container's error
	 * pages.
	 */
	@RequiredArgsConstructor
	private static class ErrorPageCustomizer implements ErrorPageRegistrar, Ordered
	{
		private final ServerProperties properties;
		private final DispatcherServletPath dispatcherServletPath;

		@Override
		public void registerErrorPages( ErrorPageRegistry errorPageRegistry ) {
			ErrorPage errorPage = new ErrorPage( this.dispatcherServletPath.getPath() + this.properties.getError().getPath() );
			errorPageRegistry.addErrorPages( errorPage );
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
}
