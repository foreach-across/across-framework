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

package com.foreach.across.test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;

/**
 * Creates an AcrossContext with a parent WebApplicationContext.
 * Requires the use of @WebAppConfiguration on the containing JUnit class.
 */
@Deprecated
@Configuration
@Import(AcrossTestContextConfiguration.class)
public class AcrossTestWebContextConfiguration implements AcrossContextConfigurer
{
	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	void verifyWebApplicationContext() {
		Assert.isTrue( applicationContext instanceof WebApplicationContext,
		               "The test ApplicationContext is not of the required WebApplicationContext type. " +
				               "Try annotating your test class with @WebAppConfiguration." );
	}

	@Override
	public void configure( AcrossContext context ) {
		context.addModule( acrossWebModule() );
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		AcrossWebModule acrossWebModule = new AcrossWebModule();
		acrossWebModule.setSupportViews( AcrossWebViewSupport.THYMELEAF );

		return acrossWebModule;
	}
}
