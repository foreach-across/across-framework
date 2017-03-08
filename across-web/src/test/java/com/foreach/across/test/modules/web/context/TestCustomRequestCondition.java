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
import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.web.mvc.condition.CustomRequestCondition;
import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestCustomRequestCondition.Config.class)
public class TestCustomRequestCondition
{
	private static final String FOREACH = "http://www.foreach.be";
	private static final String MICROSOFT = "http://www.microsoft.com";
	private static final String GOOGLE = "http://www.google.be";
	private static final String FACEBOOK = "http://www.facebook.com";

	private MockMvc mvc;

	@Autowired
	public void createMockMvc( WebApplicationContext webApplicationContext ) {
		mvc = MockMvcBuilders.webAppContextSetup( webApplicationContext ).build();
	}

	@Test
	public void defaultFallbackShouldBeReturnedForRandomUrlFromMicrosoft() throws Exception {
		fromMicrosoft( get( "/" + UUID.randomUUID().toString() ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "microsoft-default" ) );
	}

	@Test
	public void specificFallbackShouldBeReturnedForRandomUrlFromMicrosoftWithRightParam() throws Exception {
		fromMicrosoft( get( "/" + UUID.randomUUID().toString() ).param( "test", "ok" ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "microsoft-test" ) );
	}

	@Test
	public void facebookSpecificFallback() throws Exception {
		fromFacebook( get( "/some-path/" + UUID.randomUUID().toString() ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "facebook" ) );
	}

	@Test
	public void helloShouldOnlyBeReturnedWithForeachOrMicrosoftReferrer() throws Exception {
		fromGoogle( get( "/hello" ) ).andExpect( status().isNotFound() );
		fromForeach( get( "/hello" ) ).andExpect( status().isOk() ).andExpect( content().string( "hello" ) );
		fromMicrosoft( get( "/hello" ) ).andExpect( status().isOk() ).andExpect( content().string( "hello" ) );
	}

	@Test
	public void savedShouldBeReturnedOnPostWithForeachOrMicrosoftReferrer() throws Exception {
		fromGoogle( post( "/hello" ) ).andExpect( status().isNotFound() );
		fromForeach( post( "/hello" ) ).andExpect( status().isOk() ).andExpect( content().string( "saved" ) );
		fromMicrosoft( get( "/hello" ) ).andExpect( status().isOk() ).andExpect( content().string( "hello" ) );
	}

	@Test
	public void googleShouldBeReturnedOnlyWithGoogleReferrer() throws Exception {
		fromForeach( get( "/google" ) ).andExpect( status().isNotFound() );
		fromForeach( post( "/google" ) ).andExpect( status().isNotFound() );
		fromGoogle( get( "/google" ) ).andExpect( status().isOk() ).andExpect( content().string( "google" ) );
		fromGoogle( post( "/google" ) ).andExpect( status().isOk() ).andExpect( content().string( "google" ) );
	}

	@Test
	public void normalRequestMappingWithoutCustom() throws Exception {
		mvc.perform( get( "/normal" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( "normal" ) );

		mvc.perform( get( "/normal" ).header( "user", "jim" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( "normal" ) );
	}

	@Test
	public void normalRequestMappingForJohn() throws Exception {
		mvc.perform( get( "/normal" ).header( "user", "john" ) )
		   .andExpect( status().isOk() )
		   .andExpect( content().string( "john" ) );
	}

	@Test
	public void normalRequestMappingForJimAndGoogle() throws Exception {
		fromGoogle( get( "/normal" ).header( "user", "jim" ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "jim-google" ) );
	}

	@Test
	public void testThatCustomRequestConditionDoesNotMatch() throws Exception {
		fromForeach( get( "/requestcondition" ) )
				.andExpect( status().is( HttpStatus.NOT_FOUND.value() ) );
	}

	@Test
	public void testThatCustomRequestConditionMatches() throws Exception {
		fromGoogle( get( "/requestcondition" ) )
				.andExpect( status().isOk() ).andExpect( content().string( "requestconditioncontent" ) );
	}

	@Test
	public void neverMatchedCondition() throws Exception {
		fromGoogle( get( "/neverMatched" ) )
				.andExpect( status().isNotFound() );
	}

	private ResultActions fromForeach( MockHttpServletRequestBuilder builder ) throws Exception {
		return mvc.perform( builder.header( HttpHeaders.REFERER, FOREACH ) );
	}

	private ResultActions fromGoogle( MockHttpServletRequestBuilder builder ) throws Exception {
		return mvc.perform( builder.header( HttpHeaders.REFERER, GOOGLE ) );
	}

	private ResultActions fromMicrosoft( MockHttpServletRequestBuilder builder ) throws Exception {
		return mvc.perform( builder.header( HttpHeaders.REFERER, MICROSOFT ) );
	}

	private ResultActions fromFacebook( MockHttpServletRequestBuilder builder ) throws Exception {
		return mvc.perform( builder.header( HttpHeaders.REFERER, FACEBOOK ) );
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

		@Bean
		public ReferrerController referrerController() {
			return new ReferrerController();
		}

		@Bean
		public CustomOnlyController customOnlyController() {
			return new CustomOnlyController();
		}
	}

	/**
	 * Dummy mapping that will only match if any of the referrers are set.
	 * If both type and method level annotation is present, the method level annotation attributes will replace
	 * the type level attributes.
	 */
	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@CustomRequestMapping(ReferrerRequestCondition.class)
	private @interface ReferrerMapping
	{
		String[] value();
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@CustomRequestMapping(UserRequestCondition.class)
	private @interface UserMapping
	{
		String value();
	}

	private static class ReferrerRequestCondition extends AbstractCustomRequestCondition<ReferrerRequestCondition>
	{
		private String[] referrers = new String[0];

		@Override
		public void setAnnotatedElement( AnnotatedElement element ) {
			referrers = AnnotatedElementUtils.findMergedAnnotation( element, ReferrerMapping.class ).value();
		}

		@Override
		protected Collection<String> getContent() {
			return Arrays.asList( referrers );
		}

		@Override
		protected String getToStringInfix() {
			return " || ";
		}

		@Override
		public ReferrerRequestCondition combine( ReferrerRequestCondition other ) {
			return other;
		}

		@Override
		public ReferrerRequestCondition getMatchingCondition( HttpServletRequest request ) {
			if ( StringUtils.containsAny( request.getHeader( HttpHeaders.REFERER ), referrers ) ) {
				return this;
			}
			return null;
		}

		@Override
		public int compareTo( ReferrerRequestCondition other, HttpServletRequest request ) {
			return 0;
		}
	}

	private static class UserRequestCondition extends AbstractCustomRequestCondition<UserRequestCondition>
	{
		private String username;

		@Override
		public void setAnnotatedElement( AnnotatedElement element ) {
			username = AnnotatedElementUtils.findMergedAnnotation( element, UserMapping.class ).value();
		}

		@Override
		protected Collection<?> getContent() {
			return Collections.singleton( username );
		}

		@Override
		protected String getToStringInfix() {
			return "";
		}

		@Override
		public UserRequestCondition combine( UserRequestCondition other ) {
			return other;
		}

		@Override
		public UserRequestCondition getMatchingCondition( HttpServletRequest request ) {
			return StringUtils.equals( request.getHeader( "user" ), username ) ? this : null;
		}

		@Override
		public int compareTo( UserRequestCondition other, HttpServletRequest request ) {
			return 0;
		}
	}

	private static class NeverMatchedRequestCondition implements CustomRequestCondition<NeverMatchedRequestCondition>
	{
		@Override
		public void setAnnotatedElement( AnnotatedElement element ) {
		}

		@Override
		public NeverMatchedRequestCondition combine( NeverMatchedRequestCondition other ) {
			return null;
		}

		@Override
		public NeverMatchedRequestCondition getMatchingCondition( HttpServletRequest request ) {
			return null;
		}

		@Override
		public int compareTo( NeverMatchedRequestCondition other, HttpServletRequest request ) {
			return 0;
		}
	}

	@Controller
	@SuppressWarnings("unused")
	protected static class TestController
	{
		@RequestMapping("/normal")
		@ResponseBody
		public String normalRequestMapping() {
			return "normal";
		}

		@UserMapping("john")
		@RequestMapping("/normal")
		@ResponseBody
		public String normalForJohnRequestMapping() {
			return "john";
		}

		@UserMapping("jim")
		@ReferrerMapping(GOOGLE)
		@RequestMapping("/normal")
		@ResponseBody
		public String normalForJimAndGoogleRequestMapping() {
			return "jim-google";
		}

		@RequestMapping("/requestcondition")
		@ResponseBody
		@ReferrerMapping(GOOGLE)
		public String requestconditionMapping() {
			return "requestconditioncontent";
		}

		@RequestMapping(value = "/neverMatched", method = { RequestMethod.GET, RequestMethod.HEAD })
		@ResponseBody
		@ReferrerMapping(GOOGLE)
		@CustomRequestMapping(NeverMatchedRequestCondition.class)
		public String multipleRequestconditionMapping() {
			return "neverMatched";
		}
	}

	@SuppressWarnings("unused")
	@RestController
	@ReferrerMapping({ FOREACH, MICROSOFT })
	protected static class ReferrerController
	{
		@GetMapping("/hello")
		public String hello() {
			return "hello";
		}

		@PostMapping("/hello")
		public String save() {
			return "saved";
		}

		@RequestMapping("/google")
		@ReferrerMapping(GOOGLE)
		public String google() {
			return "google";
		}

		@ReferrerMapping(FACEBOOK)
		public String allFromFacebook() {
			return "facebook";
		}
	}

	@SuppressWarnings("unused")
	@RestController
	protected static class CustomOnlyController
	{
		@ReferrerMapping(MICROSOFT)
		public String allFromMicrosoft() {
			return "microsoft-default";
		}

		@GetMapping(params = "test=ok")
		@ReferrerMapping(MICROSOFT)
		public String testFromMicrosoft() {
			return "microsoft-test";
		}
	}
}