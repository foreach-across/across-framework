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
package test.scan;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.scan.extensions.BeanFourAndFiveConfiguration;
import test.scan.extensions.SomeBeanInterface;
import test.scan.moduleExtendingValidModule.ModuleExtendingValidModule;
import test.scan.packageOne.ValidModule;
import test.scan.packageTwo.OtherValidModule;

import java.util.List;

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
		// bean one is also present because it is defined in the config package, which is scanned for regular
		// configurations as well - @ModuleConfiguration is also a regular configuration
		assertTrue( beanRegistry.moduleContainsLocalBean( ModuleExtendingValidModule.NAME, "beanOne" ) );
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

	@Test
	public void twoBeansShouldExistFromTheSameBeanConfigurations() {
		List<SomeBeanInterface> beans = beanRegistry.getBeansOfType( SomeBeanInterface.class, true );
		assertEquals( 2, beans.size() );
	}

	@Configuration
	@EnableAcrossContext(
			modules = { ValidModule.NAME, OtherValidModule.NAME, ModuleExtendingValidModule.NAME },
			moduleConfigurationPackages = "test.scan.noRealPackage",
			moduleConfigurationPackageClasses = BeanFourAndFiveConfiguration.class
	)
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			assertArrayEquals(
					new String[] { "test.scan.noRealPackage",
					               "test.scan.extensions" },
					context.getModuleConfigurationScanPackages()
			);
		}
	}
}
