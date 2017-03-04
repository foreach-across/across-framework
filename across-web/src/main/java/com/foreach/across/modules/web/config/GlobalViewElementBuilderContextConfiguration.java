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

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContextInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Responsible for adding the {@link com.foreach.across.modules.web.ui.ViewElementBuilderContextInterceptor}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ConditionalOnProperty(value = "acrossWebModule.registerGlobalBuilderContext")
@OrderInModule(2)
@Configuration
public class GlobalViewElementBuilderContextConfiguration extends PrefixingHandlerMappingConfigurerAdapter
{
	@Override
	public boolean supports( String mapperName ) {
		return AcrossWebModule.NAME.equals( mapperName );
	}

	@Override
	public void addInterceptors( InterceptorRegistry interceptorRegistry ) {
		interceptorRegistry.addInterceptor( viewElementBuilderContextInterceptor() );
	}

	@Bean
	public ViewElementBuilderContextInterceptor viewElementBuilderContextInterceptor() {
		return new ViewElementBuilderContextInterceptor();
	}
}