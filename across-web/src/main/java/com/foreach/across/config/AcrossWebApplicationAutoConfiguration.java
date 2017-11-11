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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Supports Spring Boot configuration of an {@link AcrossContext}, ensures bootstrap of the
 * {@link AcrossContext} is done before the web server starts.
 *
 * @author Arne Vandamme
 * @see AcrossApplication
 * @since 1.1.2
 */
@Configuration
@Import({ DispatcherServletAutoConfiguration.class, EmbeddedServletContainerAutoConfiguration.class, ServerPropertiesAutoConfiguration.class })
public class AcrossWebApplicationAutoConfiguration
{
	@Bean
	@ConditionalOnBean({ EmbeddedServletContainerFactory.class, SpringBootServletInitializer.class })
	public static AcrossServletContextInitializer embeddedAcrossServletContextInitializer( ConfigurableWebApplicationContext webApplicationContext ) {
		return new AcrossServletContextInitializer( webApplicationContext );
	}
}