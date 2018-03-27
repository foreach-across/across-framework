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
import com.foreach.across.core.context.info.AcrossContextInfo;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageOne.ValidModule;
import test.scan.packageTwo.YetAnotherValidModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageOne.ValidModule;
import test.scan.packageTwo.YetAnotherValidModule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestNoAutoConfigure.Config.class)
public class TestNoAutoConfigure
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void beanShouldNotHaveBeenAdded() {
		assertFalse( contextInfo.hasModule( ExtendedValidModule.NAME ) );
	}

	@Test
	public void configuredModuleShouldBePresent() {
		assertTrue( contextInfo.hasModule( ValidModule.NAME ) );
	}

	@Test
	public void scannedModuleShouldNotBePresent() {
		assertFalse( contextInfo.hasModule( YetAnotherValidModule.NAME ) );
	}

	@Configuration
	@EnableAcrossContext(autoConfigure = false, modules = YetAnotherValidModule.NAME)
	static class Config implements AcrossContextConfigurer
	{
		@Bean
		public ExtendedValidModule extendedValidModule() {
			return new ExtendedValidModule();
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new ValidModule() );
		}
	}
}
