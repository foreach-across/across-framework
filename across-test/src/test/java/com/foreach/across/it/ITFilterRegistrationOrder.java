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
import com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.test.MockAcrossServletContext;
import com.foreach.across.test.support.config.MockMvcConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
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
		System.out.println( ArrayUtils.toString( filterNames ) );
		assertArrayEquals(
				new String[] { "characterEncodingFilter", "multipartFilter", "corsFilter",
				               "resourceUrlEncodingFilter" },
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

	public static class UnorderedCustomCorsFilter extends AcrossWebDynamicServletConfigurer
	{
		@Override
		protected void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException {
			FilterRegistration.Dynamic filter = servletContext.addFilter( "corsFilter", new CorsFilter(
					new UrlBasedCorsConfigurationSource() ) );
			filter.addMappingForUrlPatterns( EnumSet.allOf( DispatcherType.class ), false, "/*" );
		}

		@Override
		protected void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException {
		}
	}
}
