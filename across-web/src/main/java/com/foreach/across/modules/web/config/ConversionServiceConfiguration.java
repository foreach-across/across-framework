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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.convert.StringToDateTimeConverter;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.servlet.handler.ConversionServiceExposingInterceptor;

/**
 * Creates a default {@link org.springframework.format.support.FormattingConversionService} bean registered
 * as *mvcConversionService* if none is available from the parent context.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@Configuration
public class ConversionServiceConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( ConversionServiceConfiguration.class );

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Bean(name = AcrossWebModule.CONVERSION_SERVICE_BEAN)
	@Exposed
	@ConditionalOnMissingBean(name = AcrossWebModule.CONVERSION_SERVICE_BEAN)
	public FormattingConversionService mvcConversionService() {
		if ( beanRegistry.containsBean( ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME ) ) {
			Object conversionService
					= beanRegistry.getBean( ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME );

			if ( conversionService instanceof FormattingConversionService ) {
				LOG.info( "Using the default ConversionService as {}", AcrossWebModule.CONVERSION_SERVICE_BEAN );
				return (FormattingConversionService) conversionService;
			}
		}

		LOG.info(
				"No ConversionService named {} found in Across context - creating and exposing a new FormattingConversionService bean",
				AcrossWebModule.CONVERSION_SERVICE_BEAN );

		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addConverter( new StringToDateTimeConverter( conversionService ) );

		return conversionService;
	}

	/**
	 * Exposes the ConversionService to all prefixing mappers by default.
	 *
	 * @author Arne Vandamme
	 */
	@Configuration
	@OrderInModule(4)
	public static class ConversionServiceExposingInterceptorConfiguration extends PrefixingHandlerMappingConfigurerAdapter
	{
		@Autowired
		@Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN)
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
}
