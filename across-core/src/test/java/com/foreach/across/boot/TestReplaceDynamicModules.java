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

import com.foreach.across.boot.postprocessor.config.SamplePostProcessorModule;
import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestReplaceDynamicModules.SampleApplication.class)
@TestPropertySource(properties = "across.displayName=My Application")
public class TestReplaceDynamicModules
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void displayNameShouldBeFixedFromAnnotation() {
		assertEquals( "Sample", contextInfo.getDisplayName() );
	}

	@Test
	public void totalModuleCountShouldBeThree() {
		assertEquals( 3, contextInfo.getModules().size() );
	}

	@Test
	public void infrastructureModuleShouldBeEmpty() {
		assertTrue( contextInfo.getModuleInfo( "SampleInfrastructureModule" )
		                       .getModule() instanceof EmptyAcrossModule );
	}

	@Test
	public void applicationModuleShouldBeAdded() {
		assertTrue( contextInfo.getModuleInfo( "SampleApplicationModule" )
		                       .getModule() instanceof DynamicAcrossModule );
	}

	@Test
	public void postProcessorModuleShouldBeScanned() {
		assertTrue( contextInfo.getModuleInfo( "SamplePostProcessorModule" )
		                       .getModule() instanceof SamplePostProcessorModule );
	}

	@AcrossApplication(modules = SamplePostProcessorModule.NAME, displayName = "Sample")
	@Configuration
	protected static class SampleApplication
	{
		@Bean
		public AcrossModule emptyInfrastructureModule() {
			return new EmptyAcrossModule( "SampleInfrastructureModule" );
		}
	}
}
