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

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.test.scan.packageOne.ValidModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestComponentScanOfModules.Config.class)
public class TestComponentScanOfModules
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void moduleShouldBeScanned() {
		assertTrue( contextInfo.isBootstrapped() );
		assertEquals( 2, contextInfo.getModules().size() );
		assertTrue( contextInfo.hasModule( "someName" ) );
	}

	@Configuration
	@ComponentScan(basePackageClasses = ValidModule.class)
	@EnableAcrossContext
	static class Config
	{
	}
}
