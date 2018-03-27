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
package com.foreach.across.test.application;

import com.foreach.across.test.application.app.DummyApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DummyApplication.class)
@TestPropertySource(properties = { "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration",
                                   "server.error.includeStacktrace=ALWAYS" })
public class TestNoErrorPageHandling
{
	private final TestRestTemplate restTemplate = new TestRestTemplate();

	@Value("${local.server.port}")
	private int port;

	@Test
	public void webserverErrorForRuntimeException() {
		assertThat( getAsHtml( "/exception" ) )
				.doesNotContain( "something broke" )
				.contains( "General runtime exception" )
				.contains( "HTTP Status 500" );
	}

	@Test
	public void webserverErrorForUnauthorized() {
		assertThat( getAsHtml( "/unauthorized" ) )
				.doesNotContain( "you are not authorized" )
				.contains( "HTTP Status 401" );
	}

	@Test
	public void webserverErrorForPageNotFound() {
		assertThat( getAsHtml( "/page-does-not-exist" ) )
				.doesNotContain( "no explicit mapping" )
				.contains( "HTTP Status 404" );
	}

	private String getAsHtml( String relativePath ) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept( Collections.singletonList( MediaType.TEXT_HTML ) );
		HttpEntity<?> entity = new HttpEntity<>( headers );

		return restTemplate.exchange( url( relativePath ), HttpMethod.GET, entity, String.class ).getBody();
	}

	private String url( String relativePath ) {
		return "http://localhost:" + port + relativePath;
	}
}
