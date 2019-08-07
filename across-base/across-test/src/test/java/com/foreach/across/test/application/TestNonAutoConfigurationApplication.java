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

import com.foreach.across.config.AcrossWebApplicationAutoConfiguration;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.application.app.OtherDummyApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { OtherDummyApplication.class,
                                                                                        AcrossWebApplicationAutoConfiguration.class })
public class TestNonAutoConfigurationApplication
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	private ListableBeanFactory beanFactory;

	@Test
	public void modulesShouldBeRegistered() {
		assertTrue( contextInfo.hasModule( "emptyModule" ) );
		assertTrue( contextInfo.hasModule( AcrossWebModule.NAME ) );
		assertTrue( contextInfo.hasModule( "OtherDummyApplicationModule" ) );
		assertTrue( contextInfo.hasModule( "OtherDummyInfrastructureModule" ) );

		assertFalse( contextInfo.hasModule( "OtherDummyPostProcessorModule" ) );
	}

	@Test
	public void configurationPropertiesBeanShouldNotExist() {
		Assertions.assertThrows( NoSuchBeanDefinitionException.class, () -> {
			assertNull( BeanFactoryUtils.beanOfType( beanFactory, ConfigurationPropertiesAutoConfiguration.class ) );
		} );
	}
}
