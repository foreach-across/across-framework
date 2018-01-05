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

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ErrorPageRegistrar;
import org.springframework.boot.web.servlet.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

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
public class AcrossWebModuleAutoConfiguration
{
	private final ServerProperties serverProperties;

	/**
	 * Configure the error pages.
	 */
	@Bean
	public ErrorPageCustomizer errorPageCustomizer() {
		return new ErrorPageCustomizer( this.serverProperties );
	}

	/**
	 * {@link EmbeddedServletContainerCustomizer} that configures the container's error
	 * pages.
	 */
	private static class ErrorPageCustomizer implements ErrorPageRegistrar, Ordered
	{
		private final ServerProperties properties;

		ErrorPageCustomizer( ServerProperties properties ) {
			this.properties = properties;
		}

		@Override
		public void registerErrorPages( ErrorPageRegistry errorPageRegistry ) {
			ErrorPage errorPage = new ErrorPage( this.properties.getServletPrefix() + this.properties.getError().getPath() );
			errorPageRegistry.addErrorPages( errorPage );
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
}
