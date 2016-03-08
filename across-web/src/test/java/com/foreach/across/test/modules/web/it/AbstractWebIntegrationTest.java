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
package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.AcrossWebApplicationConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for a fully bootstrapped webapplication in embedded tomcat.
 * The configuration containing the AcrossContext should be added in an implementing test using
 * {@link org.springframework.test.context.ContextConfiguration}.
 *
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest(randomPort = true)
@SpringApplicationConfiguration(classes = AcrossWebApplicationConfiguration.class)
public abstract class AbstractWebIntegrationTest
{
	@Autowired
	protected EmbeddedWebApplicationContext server;

	protected RestTemplate template = new RestTemplate();
	protected String host;

	@Before
	public void determineHost() {
		host = "http://localhost:" + server.getEmbeddedServletContainer().getPort();
	}

	protected String url( String relativePath ) {
		return host + relativePath;
	}

	protected String get( String relativePath ) {
		return template.getForEntity( url( relativePath ), String.class ).getBody();
	}

	protected HttpHeaders headers( String relativePath ) {
		return template.getForEntity( url( relativePath ), String.class ).getHeaders();
	}

	protected boolean notFound( String relativePath ) {
		try {
			get( relativePath );
		}
		catch ( HttpClientErrorException hcee ) {
			return HttpStatus.NOT_FOUND.equals( hcee.getStatusCode() );
		}
		return false;
	}
}
