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
package com.foreach.across.boot;

import com.foreach.across.boot.application.RootComponent;
import com.foreach.across.boot.postprocessor.config.SamplePostProcessorModule;
import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration
@TestPropertySource(properties = "across.displayName=My Application")
public class TestAcrossApplicationBootstrap
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired
	private AcrossContextBeanRegistry contextBeanRegistry;

	@Test
	public void displayNameShouldBeSetFromProperties() {
		assertEquals( "My Application", contextInfo.getDisplayName() );
	}

	@Test
	public void infrastructureModuleShouldBeAdded() {
		assertEquals( 1, contextInfo.getModuleIndex( "SampleInfrastructureModule" ) );
		assertEquals( "sampleInfrastructure", contextInfo.getModuleInfo( "SampleInfrastructureModule" )
		                                                 .getResourcesKey() );
		assertTrue( contextInfo.getModuleInfo( "SampleInfrastructureModule" )
		                       .getModule() instanceof DynamicAcrossModule );
	}

	@Test
	public void applicationModuleShouldBeAdded() {
		assertEquals( 2, contextInfo.getModuleIndex( "SampleApplicationModule" ) );
		assertEquals( "sample", contextInfo.getModuleInfo( "SampleApplicationModule" ).getResourcesKey() );
		assertTrue( contextInfo.getModuleInfo( "SampleApplicationModule" )
		                       .getModule() instanceof DynamicAcrossModule );
		assertNotNull( contextInfo.getModuleInfo( "SampleApplicationModule" ).getApplicationContext().getBean( RootComponent.class ) );
		assertFalse( contextInfo.getModuleInfo( "SampleApplicationModule" ).getApplicationContext().containsBean( "extensionComponent" ) );
		assertFalse( contextInfo.getModuleInfo( "SampleApplicationModule" ).getApplicationContext().containsBean( "installerComponent" ) );
	}

	@Test
	public void postProcessorModuleShouldBeAddedButScanned() {
		assertEquals( 3, contextInfo.getModuleIndex( "SamplePostProcessorModule" ) );
		assertEquals( "SamplePostProcessorModule", contextInfo.getModuleInfo( "SamplePostProcessorModule" )
		                                                      .getResourcesKey() );
		assertTrue( contextInfo.getModuleInfo( "SamplePostProcessorModule" )
		                       .getModule() instanceof SamplePostProcessorModule );
	}

	@Test
	public void singleValidatorShouldBeRegisteredAndInTheRootApplicationContext() {
		Validator validator = applicationContext.getBean( Validator.class );
		assertNotNull( validator );

		assertEquals( 1, contextBeanRegistry.getBeansOfType( Validator.class, true ).size() );
		assertSame( validator, contextBeanRegistry.getBeanOfType( Validator.class ) );

		assertTrue( validator instanceof SmartValidator );
	}

	@AcrossApplication
	@Configuration
	protected static class SampleApplication
	{
	}
}