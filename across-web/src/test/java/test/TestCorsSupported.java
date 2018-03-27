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
package test;

import com.foreach.across.config.EnableAcrossContext;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import sun.net.www.protocol.http.HttpURLConnection;
import test.modules.cors.CorsModule;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Marc Vanbrabant
 */
@ContextConfiguration(classes = TestCorsSupported.Config.class)
public class TestCorsSupported extends AbstractWebIntegrationTest
{
	private RestTemplate restTemplate;
	private String URL = "/cors/78";

	@Before
	public void allowRestrictedHeaders() throws NoSuchFieldException, IllegalAccessException {
		restTemplate = new RestTemplate();
		// RestTemplate uses HttpURLConnection, which disallows CORS headers by default.
		// Since it is a private static final field, we need some trickery to make RestTemplate work okay with CORS
		setAllowRestrictedHeaders( true );
	}

	@After
	public void resetRestrictedHeaders() throws NoSuchFieldException, IllegalAccessException {
		setAllowRestrictedHeaders(
				Boolean.parseBoolean( System.getProperty( "sun.net.http.allowRestrictedHeaders" ) ) );
	}

	public void setAllowRestrictedHeaders( Object value ) throws NoSuchFieldException, IllegalAccessException {
		Field field = ReflectionUtils.findField( HttpURLConnection.class, "allowRestrictedHeaders" );
		ReflectionUtils.makeAccessible( field );
		Field modifierField = Field.class.getDeclaredField( "modifiers" );
		ReflectionUtils.makeAccessible( modifierField );
		modifierField.setInt( field, field.getModifiers() & ~Modifier.FINAL );
		ReflectionUtils.setField( field, null, value );
	}

	@Test
	public void preflightIsNotAllowedForOtherOrigin() throws Exception {
		restTemplate.setErrorHandler( new NoOpResponseErrorHandler() );
		ResponseEntity<String> response = restTemplate.exchange( url( URL ), HttpMethod.OPTIONS,
		                                                         new HttpEntity( new HttpHeaders()
		                                                         {
			                                                         {
				                                                         set( HttpHeaders.ORIGIN, "http://foo.bar" );
				                                                         set( HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
				                                                              "POST" );
			                                                         }
		                                                         } ), String.class, Collections.emptyMap() );
		assertEquals( HttpStatus.FORBIDDEN, response.getStatusCode() );
	}

	@Test
	public void preflightAndCorsRequestShouldWork() throws Exception {
		ResponseEntity<String> response = restTemplate.exchange( url( URL ), HttpMethod.OPTIONS,
		                                                         new HttpEntity( new HttpHeaders()
		                                                         {
			                                                         {
				                                                         set( HttpHeaders.ORIGIN,
				                                                              "http://domain2.com" );
				                                                         set( HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
				                                                              "GET" );
			                                                         }
		                                                         } ), String.class, Collections.emptyMap() );
		assertEquals( HttpStatus.OK, response.getStatusCode() );
		HttpHeaders httpHeaders = response.getHeaders();
		assertEquals( "GET,HEAD,POST", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS ).get( 0 ) );
		assertEquals( "http://domain2.com", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN ).get( 0 ) );
		assertEquals( "89", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_MAX_AGE ).get( 0 ) );

		ResponseEntity<Long> controllerResponse = restTemplate.getForEntity( url( URL ), Long.class,
		                                                                     Collections.emptyMap() );
		assertEquals( HttpStatus.OK, controllerResponse.getStatusCode() );
		assertEquals( Long.valueOf( 78 ), controllerResponse.getBody() );
	}

	@Test
	public void preflightAndCorsRequestShouldWorkForGlobalConfig() throws Exception {
		Long randomLong = RandomUtils.nextLong( 1, 100 );
		ResponseEntity<String> response = restTemplate.exchange( url( "/cors/global/" + randomLong ),
		                                                         HttpMethod.OPTIONS, new HttpEntity( new HttpHeaders()
				{
					{
						set( HttpHeaders.ORIGIN, "http://thirddomain.com" );
						set( HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET" );
					}
				} ), String.class, Collections.emptyMap() );
		assertEquals( HttpStatus.OK, response.getStatusCode() );
		HttpHeaders httpHeaders = response.getHeaders();
		assertEquals( "GET", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS ).get( 0 ) );
		assertEquals( "http://thirddomain.com", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN ).get( 0 ) );
		assertEquals( "254", httpHeaders.get( HttpHeaders.ACCESS_CONTROL_MAX_AGE ).get( 0 ) );

		ResponseEntity<Long> controllerResponse = restTemplate.getForEntity( url( "/cors/global/" + randomLong ),
		                                                                     Long.class, Collections.emptyMap() );
		assertEquals( HttpStatus.OK, controllerResponse.getStatusCode() );
		assertEquals( randomLong, controllerResponse.getBody() );
	}

	@Test
	public void corsWorksForResourceHandlers() throws Exception {
		ResponseEntity<String> response = restTemplate.exchange(
				url( "/across/resources/js/testResources/javascript.js" ), HttpMethod.OPTIONS,
				new HttpEntity( new HttpHeaders()
				{
					{
						set( HttpHeaders.ORIGIN, "http://staticscrossdomain.com" );
						set( HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET" );
					}
				} ), String.class, Collections.emptyMap() );
		assertEquals( HttpStatus.OK, response.getStatusCode() );
	}

	@Configuration
	@EnableAcrossContext(modules = CorsModule.NAME)
	protected static class Config extends WebMvcConfigurerAdapter
	{
	}
}
