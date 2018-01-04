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
import com.foreach.across.config.AcrossDynamicModulesConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration
public class TestMultipleDynamicModuleConfigurers
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void sampleModulesShouldBeAdded() {
		assertTrue( contextInfo.getModuleInfo( "SampleInfrastructureModule" )
		                       .getModule() instanceof DynamicAcrossModule );
		assertTrue( contextInfo.getModuleInfo( "SampleApplicationModule" )
		                       .getModule() instanceof DynamicAcrossModule );
		assertTrue( contextInfo.getModuleInfo( "SamplePostProcessorModule" )
		                       .getModule() instanceof SamplePostProcessorModule );
	}

	@Test
	public void otherModulesShouldBeAdded() {
		assertTrue( contextInfo.getModuleInfo( "OtherInfrastructureModule" )
		                       .getModule() instanceof EmptyAcrossModule );
		assertTrue( contextInfo.getModuleInfo( "OtherApplicationModule" )
		                       .getModule() instanceof DynamicAcrossModule );
		assertTrue( contextInfo.getModuleInfo( "OtherPostProcessorModule" )
		                       .getModule() instanceof DynamicAcrossModule );
	}

	@Configuration
	@EnableAcrossContext
	public static class SampleApplication
	{
		@Bean
		public AcrossDynamicModulesConfigurer sampleDynamicModules() {
			return new AcrossDynamicModulesConfigurer( SampleApplication.class );
		}

		@Bean
		public AcrossDynamicModulesConfigurer otherDynamicModules() {
			return new AcrossDynamicModulesConfigurer(
					TestMultipleDynamicModuleConfigurers.class.getPackage().getName(),
					"Other"
			);
		}

		@Bean
		public EmptyAcrossModule otherInfrastructureModule() {
			return new EmptyAcrossModule( "OtherInfrastructureModule" );
		}
	}
}
