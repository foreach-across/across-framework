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
package com.foreach.across.modules.web.config.resourceurls;

import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

@Configuration
@OrderInModule(4)
@AcrossCondition("settings.autoConfigureRecourceUrls")
public class ResourceUrlProviderExposingInterceptorConfiguration extends PrefixingHandlerMappingConfigurerAdapter
{
	@Override
	public boolean supports( String mapperName ) {
		return AcrossWebModule.NAME.equals( mapperName );
	}

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( resourceUrlProviderExposingInterceptor( mvcResourceUrlProvider() ) );
	}

	@Bean
	public ResourceUrlProvider mvcResourceUrlProvider() {
		return new ResourceUrlProvider();
	}

	@Bean
	public ResourceUrlProviderExposingInterceptor resourceUrlProviderExposingInterceptor( ResourceUrlProvider resourceUrlProvider ) {
		return new ResourceUrlProviderExposingInterceptor( resourceUrlProvider );
	}
}
