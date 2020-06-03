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
package com.foreach.across.boot;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.ModuleBootstrapStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer.CONTEXT_INFRASTRUCTURE_MODULE;
import static com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestNoDynamicModules.SampleApplication.class)
public class TestNoDynamicModules
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	void displayNameShouldBeGenerated() {
		assertEquals( "TestNoDynamicModules$SampleApplication", contextInfo.getDisplayName() );
	}

	@Test
	void noModulesShouldBeBootstrapped() {
		assertTrue( contextInfo.getBootstrappedModules().isEmpty() );
	}

	@Test
	void allModulesShouldBeSkipped() {
		Assertions.assertThat( contextInfo.getModules() ).hasSize( 2 );
		Assertions.assertThat( contextInfo.getModuleInfo( CONTEXT_INFRASTRUCTURE_MODULE ).getBootstrapStatus() ).isEqualTo( ModuleBootstrapStatus.Skipped );
		Assertions.assertThat( contextInfo.getModuleInfo( CONTEXT_POSTPROCESSOR_MODULE ).getBootstrapStatus() ).isEqualTo( ModuleBootstrapStatus.Skipped );
	}

	@AcrossApplication(enableDynamicModules = false)
	@Configuration
	protected static class SampleApplication
	{
	}
}
