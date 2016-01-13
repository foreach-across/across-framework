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

import com.foreach.across.core.context.ClassPathScanningModuleConfigurationProvider;
import com.foreach.across.core.context.ModuleConfigurationSet;
import com.foreach.across.test.scan.moduleExtendingValidModule.ModuleExtendingValidModule;
import com.foreach.across.test.scan.moduleExtendingValidModule.config.BeanOneConfiguration;
import com.foreach.across.test.scan.moduleExtendingValidModule.extensions.BeanTwoConfiguration;
import com.foreach.across.test.scan.packageOne.ValidModule;
import com.foreach.across.test.scan.packageTwo.OtherValidModule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningModuleConfigurationProvider
{
	@Test
	public void noAnnotatedClasses() {
		ClassPathScanningModuleConfigurationProvider provider = new ClassPathScanningModuleConfigurationProvider();
		ModuleConfigurationSet configurationSet = provider.scan( "illegal" );

		assertNotNull( configurationSet );
		assertEquals( 0, configurationSet.getAnnotatedClasses( ValidModule.NAME ).length );
		assertEquals( 0, configurationSet.getAnnotatedClasses( OtherValidModule.NAME ).length );
		assertEquals( 0, configurationSet.getAnnotatedClasses( "badModule" ).length );
	}

	@Test
	public void annotatedClassForAllModulesExceptOne() {
		ClassPathScanningModuleConfigurationProvider provider = new ClassPathScanningModuleConfigurationProvider();
		ModuleConfigurationSet configurationSet = provider.scan(
				"com.foreach.across.test.scan.moduleExtendingValidModule.config"
		);

		assertNotNull( configurationSet );
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class }, configurationSet.getAnnotatedClasses( ValidModule.NAME )
		);
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class },
				configurationSet.getAnnotatedClasses( OtherValidModule.NAME )
		);
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class }, configurationSet.getAnnotatedClasses( "badModule" )
		);

		// excluded
		assertEquals( 0, configurationSet.getAnnotatedClasses( ModuleExtendingValidModule.NAME ).length );
	}

	@Test
	public void multiplePackages() {
		ClassPathScanningModuleConfigurationProvider provider = new ClassPathScanningModuleConfigurationProvider();
		ModuleConfigurationSet configurationSet = provider.scan(
				"com.foreach.across.test.scan.moduleExtendingValidModule.config",
				"com.foreach.across.test.scan.moduleExtendingValidModule.extensions"
		);

		assertNotNull( configurationSet );
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class, BeanTwoConfiguration.class },
				configurationSet.getAnnotatedClasses( ValidModule.NAME )
		);
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class },
				configurationSet.getAnnotatedClasses( OtherValidModule.NAME )
		);
		assertArrayEquals(
				new Class[] { BeanOneConfiguration.class }, configurationSet.getAnnotatedClasses( "badModule" )
		);
		assertEquals( 0, configurationSet.getAnnotatedClasses( ModuleExtendingValidModule.NAME ).length );
	}
}
