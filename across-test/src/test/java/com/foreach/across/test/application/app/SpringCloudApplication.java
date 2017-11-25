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
package com.foreach.across.test.application.app;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.modules.web.AcrossWebModule;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Sample application configuration for Spring Boot execution which mimics the Spring Cloud Context bootstrap.
 *
 * @author Marc Vanbrabant
 * @since 3.0.0
 */
@AcrossApplication(modules = AcrossWebModule.NAME)
public class SpringCloudApplication
{
	@Bean
	public AcrossModule emptyModule() {
		return new SpringCloudApplicationModule()
		{
			@Override
			public void prepareForBootstrap( ModuleBootstrapConfig currentModule, AcrossBootstrapConfig contextConfig ) {
				AcrossContext acrossContext = contextConfig.getContext();
				GenericApplicationContext applicationContext = (GenericApplicationContext) acrossContext.getParentApplicationContext();

				GenericApplicationContext springCloudBootstrapContext = new GenericApplicationContext();
				springCloudBootstrapContext.setId( "bootstrap" );
				BeanFactory beanFactory = (BeanFactory) ReflectionTestUtils.getField( applicationContext, "beanFactory" );
				ReflectionTestUtils.setField( beanFactory, "parentBeanFactory", null );
				applicationContext.setParent( springCloudBootstrapContext );

				springCloudBootstrapContext.refresh();
				springCloudBootstrapContext.start();

				super.prepareForBootstrap( currentModule, contextConfig );
			}
		};
	}

	public static class SpringCloudApplicationModule extends AcrossModule
	{

		@Override
		public String getName() {
			return "SpringCloudApplicationModule";
		}
	}
}
