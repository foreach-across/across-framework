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

import com.foreach.across.AcrossPlatform;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import test.modules.TestModules;
import test.modules.testResources.TestResourcesModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = TestDefaultWebjarConfiguration.Config.class)
public class TestDefaultWebjarConfiguration extends AbstractWebIntegrationTest
{
	@Autowired
	private WebAppPathResolver pathResolver;
	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void defaultWebjarsPathIsSlashWebjars() {
		String resolvedPath = pathResolver.path( "@webjars:/jquery/3.3.0/jquery.js" );
		assertEquals( "/webjars/jquery/3.3.0/jquery.js", resolvedPath );
		ResponseEntity<String> response = restTemplate.getForEntity( host + resolvedPath, String.class );
		assertEquals( HttpStatus.OK, response.getStatusCode() );
		assertTrue( StringUtils.contains( response.getBody(), "jQuery JavaScript Library v3.3.0" ) );
		assertEquals( "application/javascript;charset=UTF-8", response.getHeaders().getFirst( "Content-Type" ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = TestResourcesModule.NAME,
			modulePackageClasses = { AcrossPlatform.class, TestModules.class }
	)
	public static class Config extends WebMvcConfigurerAdapter
	{
	}
}
