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

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.application.app.DummyApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Full stack bootstrap with embedded tomcat.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DummyApplication.class)
public class TestSpringBootWebIntegration
{
	private final TestRestTemplate restTemplate = new TestRestTemplate();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	private ListableBeanFactory beanFactory;

	@Test
	public void modulesShouldBeRegistered() {
		assertTrue( contextInfo.hasModule( "emptyModule" ) );
		assertTrue( contextInfo.hasModule( AcrossWebModule.NAME ) );
		assertTrue( contextInfo.hasModule( "DummyApplicationModule" ) );
		assertTrue( contextInfo.hasModule( "DummyInfrastructureModule" ) );

		assertFalse( contextInfo.hasModule( "DummyPostProcessorModule" ) );
	}

	@Test
	public void controllersShouldSayHello() {
		assertEquals( "application says hello", get( "/application" ) );
		assertEquals( "infrastructure says hello", get( "/infrastructure" ) );
	}

	@Test
	public void versionedResourceShouldBeReturned() {
		assertEquals( "hùllµ€", get( "/res/static/boot-1.0/testResources/test.txt" ) );
	}

	@Test
	public void configurationPropertiesBeanShouldNotExist() {
		assertNotNull( BeanFactoryUtils.beanOfType( beanFactory, ConfigurationPropertiesAutoConfiguration.class ) );
	}

	@Test
	public void defaultAutoConfigurationPackageShouldNotBeRegisteredInMainContext() {
		assertEquals( Collections.emptyList(), AutoConfigurationPackages.get( beanFactory ) );
	}

	@Test
	public void autoConfigurationPackageShouldBeApplicationModule() {
		String applicationModulePackage = DummyApplication.class.getPackage().getName() + ".application";
		assertEquals(
				Collections.singletonList( applicationModulePackage ),
				AutoConfigurationPackages.get( contextInfo.getModuleInfo( "DummyApplicationModule" ).getApplicationContext() )
		);
	}

	private String get( String relativePath ) {
		return restTemplate.getForEntity( url( relativePath ), String.class ).getBody();
	}

	private String url( String relativePath ) {
		return "http://localhost:" + port + relativePath;
	}
}
