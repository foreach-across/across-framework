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
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.handler.ConversionServiceExposingInterceptor;

/**
 * Exposes the ConversionService to all mappers by default.
 *
 * @author Arne Vandamme
 */
@Configuration
@OrderInModule(3)
public class ConversionServiceExposingInterceptorConfiguration extends PrefixingHandlerMappingConfigurerAdapter
{
	@Autowired
	@Qualifier("mvcConversionService")
	private ConversionService mvcConversionService;

	@Override
	public boolean supports( String mapperName ) {
		return true;
	}

	@Override
	public void addInterceptors( InterceptorRegistry interceptorRegistry ) {
		interceptorRegistry.addInterceptor( new ConversionServiceExposingInterceptor( mvcConversionService ) );
	}
}
