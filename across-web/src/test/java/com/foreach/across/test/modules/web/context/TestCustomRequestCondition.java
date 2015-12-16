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
package com.foreach.across.test.modules.web.context;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.CustomRequestCondition;
import com.foreach.across.modules.web.mvc.CustomRequestConditionMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestCustomRequestCondition.Config.class)
public class TestCustomRequestCondition
{
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void testThatNormalRequestMappingWork() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup( webApplicationContext ).build();
		mvc.perform( get( "/normal" ).header( HttpHeaders.REFERER, "http://www.foreach.be" ) ).andExpect(
				status().isOk() ).andExpect( content().string(
				"normalcontent" ) );
	}

	@Test
	public void testThatCustomRequestConditionDoesNotMatch() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup( webApplicationContext ).build();
		mvc.perform( get( "/requestcondition" ).header( HttpHeaders.REFERER,
		                                                "http://www.foreach.be" ) ).andExpect( status().is(
				HttpStatus.NOT_FOUND.value() ) );
	}

	@Test
	public void testThatCustomRequestConditionMatches() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup( webApplicationContext ).build();
		mvc.perform( get( "/requestcondition" ).header( HttpHeaders.REFERER,
		                                                "http://www.google.be" ) ).andExpect( status().isOk() )
		   .andExpect( content().string( "requestconditioncontent" ) );
	}

	@Test
	public void testThatMultipleCustomRequestConditionMatchesFirstOccurence() throws Exception {
		MockMvc mvc = MockMvcBuilders.webAppContextSetup( webApplicationContext ).build();
		mvc.perform( get( "/multiplerequestcondition" ).header( HttpHeaders.REFERER,
		                                                "http://www.google.be" ) ).andExpect( status().isOk() )
		   .andExpect( content().string( "multiplerequestconditioncontent" ) );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config extends WebMvcConfigurerAdapter implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}

		@Bean
		public TestController testController() {
			return new TestController();
		}
	}

	@Controller
	@SuppressWarnings( "unused" )
	protected static class TestController
	{
		@RequestMapping("/normal")
		@ResponseBody
		public String normalRequestMapping() {
			return "normalcontent";
		}

		@RequestMapping("/requestcondition")
		@ResponseBody
		@CustomRequestCondition(conditions = ReferrerRequestCondition.class)
		public String requestconditionMapping() {
			return "requestconditioncontent";
		}

		@RequestMapping(value = "/multiplerequestcondition", method = { RequestMethod.GET, RequestMethod.HEAD } )
		@ResponseBody
		@CustomRequestCondition(conditions = { ReferrerRequestCondition.class, NeverMatchedRequestCondition.class } )
		public String multipleRequestconditionMapping() {
			return "multiplerequestconditioncontent";
		}

		private static class ReferrerRequestCondition implements CustomRequestConditionMatcher
		{
			@Override
			public boolean matches( HttpServletRequest request ) {
				return request.getHeader( HttpHeaders.REFERER ).contains( "http://www.google.be" );
			}
		}

		private static class NeverMatchedRequestCondition implements CustomRequestConditionMatcher
		{
			@Override
			public boolean matches( HttpServletRequest request ) {
				throw new RuntimeException( "This should never be called" );
			}
		}
	}
}
