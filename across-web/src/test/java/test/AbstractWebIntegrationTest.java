/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.config.AcrossServletContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import java.io.IOException;

/**
 * Base class for a fully bootstrapped webapplication in embedded tomcat.
 * The configuration containing the AcrossContext should be added in an implementing test using
 * {@link org.springframework.test.context.ContextConfiguration}.
 *
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AbstractWebIntegrationTest.AcrossWebApplicationConfiguration.class)
public abstract class AbstractWebIntegrationTest
{
	@Autowired
	protected ServletWebServerApplicationContext server;

	protected RestTemplate template = new RestTemplate();
	protected String host;

	@BeforeEach
	public void determineHost() {
		host = "http://localhost:" + server.getWebServer().getPort();
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

	protected static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler
	{
		protected NoOpResponseErrorHandler() {
		}

		public void handleError( ClientHttpResponse response ) throws IOException {
		}
	}

	@Configuration
	@Import({ DispatcherServletAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class })
	static class AcrossWebApplicationConfiguration
	{
		@Bean
		public static AcrossServletContextInitializer embeddedAcrossServletContextInitializer( ConfigurableWebApplicationContext webApplicationContext ) {
			return new AcrossServletContextInitializer( webApplicationContext );
		}
	}
}
