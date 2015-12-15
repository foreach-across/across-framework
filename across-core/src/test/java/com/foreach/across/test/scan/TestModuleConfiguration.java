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
package com.foreach.across.test.scan;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.test.scan.extensions.BeanFourAndFiveConfiguration;
import com.foreach.across.test.scan.moduleExtendingValidModule.ModuleExtendingValidModule;
import com.foreach.across.test.scan.packageOne.ValidModule;
import com.foreach.across.test.scan.packageTwo.OtherValidModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestModuleConfiguration.Config.class)
public class TestModuleConfiguration
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void moduleExtendingValidModuleShouldOnlyHaveBeanFourAndFive() {
		assertFalse( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanOne" ) );
		assertFalse( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanTwo" ) );
		assertFalse( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanThree" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanFour" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanFive" ) );
	}

	@Test
	public void allBeansShouldHaveBeenCreatedInValidModule() {
		assertTrue( beanRegistry.moduleContainsLocalBean( ValidModule.NAME, "beanOne" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ValidModule.NAME, "beanTwo" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ValidModule.NAME, "beanThree" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ValidModule.NAME, "beanFour" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( ValidModule.NAME, "beanFive" ) );
	}

	@Test
	public void beanOneAndFiveShouldAlsoHaveBeenCreatedInOtherValidModule() {
		assertTrue( beanRegistry.moduleContainsLocalBean( OtherValidModule.NAME, "beanOne" ) );
		assertFalse( beanRegistry.moduleContainsLocalBean( OtherValidModule.NAME, "beanTwo" ) );
		assertFalse( beanRegistry.moduleContainsLocalBean( OtherValidModule.NAME, "beanThree" ) );
		assertFalse( beanRegistry.moduleContainsLocalBean( OtherValidModule.NAME, "beanFour" ) );
		assertTrue( beanRegistry.moduleContainsLocalBean( OtherValidModule.NAME, "beanFive" ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = { ValidModule.NAME, OtherValidModule.NAME, ModuleExtendingValidModule.NAME },
			moduleConfigurationPackages = "com.foreach.across.test.scan.noRealPackage",
			moduleConfigurationPackageClasses = BeanFourAndFiveConfiguration.class
	)
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			assertArrayEquals(
					new String[] { "com.foreach.across.test.scan.noRealPackage",
					               "com.foreach.across.test.scan.extensions" },
					context.getModuleConfigurationScanPackages()
			);
		}
	}
}