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
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfiguration;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.*;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class ITPrefixingRequestMappingHandlerMapping
{
	@Test
	public void testThatDefaultPathIsFoundInUrlMap() throws Exception {
		try (AcrossTestContext ctx = new AcrossTestWebContext( new Config() )) {
			validateUrlMap( ctx, "/defaultpath/testrequestmapping", "/defaultpath/testrequestmappingendingwithslash/",
			                "/defaultpath/testrequestmappingwithoutendingandtrailingslash" );
		}
	}

	@Test
	public void testThatOverridenPrefixPathIsFoundInUrlMapForSlash() throws Exception {
		try (AcrossTestContext ctx = new AcrossTestWebContext( new Config( "/" ) )) {
			validateUrlMap( ctx, "/testrequestmapping", "/testrequestmappingendingwithslash/",
			                "/testrequestmappingwithoutendingandtrailingslash" );
		}
	}

	@Test
	public void testThatOverridenPrefixPathIsFoundInUrlMapForPathEndingWithSlash() throws Exception {
		try (AcrossTestContext ctx = new AcrossTestWebContext( new Config( "/otherpath/" ) )) {
			validateUrlMap( ctx, "/otherpath/testrequestmapping", "/otherpath/testrequestmappingendingwithslash/",
			                "/otherpath/testrequestmappingwithoutendingandtrailingslash" );
		}
	}

	@SuppressWarnings("unchecked")
	/**
	 * This check makes sure we have directPathMatches in {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod(String, HttpServletRequest)}
	 */
	private void validateUrlMap( AcrossTestContext ctx, String... expectedPaths ) throws Exception {
		RequestMappingHandlerMapping requestMappingHandlerMapping =
				(RequestMappingHandlerMapping) ctx.beanRegistry().getBean( "prefixingRequestMappingHandlerMapping" );
		assertNotNull( requestMappingHandlerMapping );
		MultiValueMap<String, LinkedList<?>>
				urlMap = (MultiValueMap<String, LinkedList<?>>) ReflectionTestUtils.getField(
				requestMappingHandlerMapping, "urlMap" );
		assertNotNull( urlMap );
		for ( String expectedPath : expectedPaths ) {

			LinkedList<?> mappings = (LinkedList<?>) urlMap.get( expectedPath );
			assertNotNull( "Could not find url " + expectedPath + " in urlMap", mappings );
			HttpServletRequest request = new MockHttpServletRequest( "GET", expectedPath );
			HandlerExecutionChain chain = requestMappingHandlerMapping.getHandler( request );
			assertNotNull( chain );
			assertNotNull( chain.getHandler() );
		}

	}

	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		private final String prefixPath;

		public Config() {
			this.prefixPath = "/defaultpath";
		}

		public Config( String prefixPath ) {
			this.prefixPath = prefixPath;
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
			PrefixingTestWebModule prefixingTestWebModule = new PrefixingTestWebModule();
			context.addModule( prefixingTestWebModule );
		}
	}

	@AcrossDepends(required = "AcrossWebModule")
	public static class PrefixingTestWebModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "PrefixingWebModule";
		}

		@Override
		public String getDescription() {
			return "PrefixingWebModule description";
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( PrefixingModuleWebMvcConfiguration.class ) );
		}
	}

	@Configuration
	public static class PrefixingModuleWebMvcConfiguration extends PrefixingHandlerMappingConfiguration
	{
		@Autowired
		private Config config;

		@Override
		protected String getPrefixPath() {
			return config.prefixPath;
		}

		@Override
		protected ClassFilter getHandlerMatcher() {
			return new AnnotationClassFilter( PrefixingWebController.class, true );
		}

		@Bean(name = "prefixingRequestMappingHandlerMapping")
		@Exposed
		@Override
		public PrefixingRequestMappingHandlerMapping controllerHandlerMapping() {
			return super.controllerHandlerMapping();
		}

		@Bean
		public TestPrefixingWebController testController() {
			return new TestPrefixingWebController();
		}
	}

	@PrefixingWebController
	protected static class TestPrefixingWebController
	{
		@RequestMapping("/testrequestmapping")
		@ResponseBody
		@SuppressWarnings("unused")
		public String testRequestMapping() {
			return "test";
		}

		@RequestMapping("/testrequestmappingendingwithslash/")
		@ResponseBody
		@SuppressWarnings("unused")
		public String testRequestMappingEndingWithSlash() {
			return "test";
		}

		@RequestMapping("testrequestmappingwithoutendingandtrailingslash")
		@ResponseBody
		@SuppressWarnings("unused")
		public String testRequestMappingWithoutEndingAndTrailingSlash() {
			return "test";
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Component
	@AcrossEventHandler
	public @interface PrefixingWebController
	{
	}

}
