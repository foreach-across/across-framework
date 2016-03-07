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

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebModuleSettings;
import com.foreach.across.modules.web.config.CharacterEncodingConfiguration;
import com.foreach.across.modules.web.config.multipart.MultipartResolverConfiguration;
import com.foreach.across.modules.web.servlet.AcrossMultipartFilter;
import org.junit.Test;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static com.foreach.across.modules.web.config.resources.ResourcesConfiguration.ResourceUrlEncodingFilterConfiguration;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestAcrossWebModuleBootstrap
{
	@Test
	public void acrossWebModuleDefaultFilters() {
		try (AcrossTestWebContext ctx = web().modules( AcrossWebModule.NAME ).build()) {
			MockAcrossServletContext servletContext = ctx.getServletContext();
			assertNotNull( servletContext );

			assertCharacterEncodingFilter(
					servletContext.getFilterRegistration( CharacterEncodingConfiguration.FILTER_NAME )
			);
			assertResourceUrlEncodingFilter(
					servletContext.getFilterRegistration( ResourceUrlEncodingFilterConfiguration.FILTER_NAME )
			);
			assertMultipartResolverFilter(
					servletContext.getFilterRegistration( MultipartResolverConfiguration.FILTER_NAME )
			);

			// Resource url encoding must be last
			assertEquals(
					ResourceUrlEncodingFilterConfiguration.FILTER_NAME,
					new ArrayList<>( servletContext.getFilterRegistrations().keySet() ).get( 2 )
			);
		}
	}

	private void assertCharacterEncodingFilter( MockFilterRegistration registration ) {
		assertNotNull( registration );
		assertTrue( registration.getFilter() instanceof CharacterEncodingFilter );
		assertEquals(
				Collections.singletonList(
						new MockFilterRegistration.MappingRule(
								true,
								false,
								EnumSet.of( DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR ),
								"/*"
						) ),
				registration.getMappingRules()
		);
	}

	private void assertResourceUrlEncodingFilter( MockFilterRegistration registration ) {
		assertNotNull( registration );
		assertTrue( registration.getFilter() instanceof ResourceUrlEncodingFilter );
		assertEquals(
				Collections.singletonList(
						new MockFilterRegistration.MappingRule(
								true,
								true,
								EnumSet.of( DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR ),
								"/*"
						) ),
				registration.getMappingRules()
		);
	}

	private void assertMultipartResolverFilter( MockFilterRegistration registration ) {
		assertNotNull( registration );
		assertTrue( registration.getFilter() instanceof AcrossMultipartFilter );
		assertEquals(
				Collections.singletonList(
						new MockFilterRegistration.MappingRule(
								true,
								false,
								EnumSet.of( DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR ),
								"/*"
						) ),
				registration.getMappingRules()
		);
	}

	@Test
	public void dynamicConfigurationButDisabledThroughProperties() {
		try (
				AcrossTestWebContext ctx = web()
						.property( AcrossWebModuleSettings.MULTIPART_AUTO_CONFIGURE, "false" )
						.property( "spring.http.encoding.enabled", "false" )
						.property( "acrossWebModule.resources.versioning.enabled", "false" )
						.modules( AcrossWebModule.NAME )
						.build()
		) {
			verifyNoFiltersRegistered( ctx );
		}
	}

	@Test
	public void dynamicConfigurationDisabled() {
		try (
				AcrossTestWebContext ctx = web()
						.dynamicServletContext( false )
						.modules( AcrossWebModule.NAME )
						.build()
		) {
			verifyNoFiltersRegistered( ctx );
		}
	}

	private void verifyNoFiltersRegistered( AcrossTestWebContext ctx ) {
		MockAcrossServletContext servletContext = ctx.getServletContext();
		assertNotNull( servletContext );
		assertTrue( servletContext.isInitialized() );

		assertNull( servletContext.getFilterRegistration( CharacterEncodingConfiguration.FILTER_NAME ) );
		assertNull( servletContext.getFilterRegistration( ResourceUrlEncodingFilterConfiguration.FILTER_NAME ) );
		assertNull( servletContext.getFilterRegistration( MultipartResolverConfiguration.FILTER_NAME ) );
	}
}
