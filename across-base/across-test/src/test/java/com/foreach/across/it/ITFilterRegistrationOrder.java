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
package com.foreach.across.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.MockAcrossServletContext;
import com.foreach.across.test.support.config.MockMvcConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@SpringBootTest(classes = { ITFilterRegistrationOrder.AcrossApplicationWithCustomFilterOnTheContext.class,
                            MockMvcConfiguration.class })
@AcrossWebAppConfiguration
public class ITFilterRegistrationOrder
{
	@Autowired
	private MockAcrossServletContext servletContext;

	@Test
	public void additionalCustomFilter() {
		String[] filterNames = servletContext.getFilterRegistrations().keySet().toArray( new String[0] );
		assertArrayEquals(
				new String[] { "characterEncodingFilter", "multipartFilter", "hiddenHttpMethodFilter", "formContentFilter", "requestContextFilter",
				               "resourceUrlEncodingFilter", "corsFilter" },
				filterNames
		);
	}

	@EnableAcrossContext(modules = AcrossWebModule.NAME)
	public static class AcrossApplicationWithCustomFilterOnTheContext implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( UnorderedCustomCorsFilter.class ),
			                                         ConfigurerScope.CONTEXT_ONLY );
		}
	}

	public static class UnorderedCustomCorsFilter
	{
		@Bean
		public CorsFilter corsFilter() {
			return new CorsFilter( new UrlBasedCorsConfigurationSource() );
		}
	}
}
