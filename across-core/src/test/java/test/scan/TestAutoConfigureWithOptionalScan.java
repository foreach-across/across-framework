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

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageTwo.OtherValidModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageTwo.OtherValidModule;

import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestAutoConfigureWithOptionalScan.Config.class)
public class TestAutoConfigureWithOptionalScan
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void moduleIsAdded() {
		assertTrue( contextInfo.hasModule( ExtendedValidModule.NAME ) );
	}

	@Test
	public void optionalDependencyIsAdded() {
		assertTrue( contextInfo.hasModule( OtherValidModule.NAME ) );
	}

	@Configuration
	@EnableAcrossContext(
			modules = ExtendedValidModule.NAME,
			scanForOptionalModules = true
	)
	static class Config
	{
	}
}