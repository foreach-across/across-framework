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

import com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver;
import com.foreach.across.test.scan.packageOne.ExtendedValidModule;
import com.foreach.across.test.scan.packageOne.ValidModule;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningModuleDependencyResolver
{
	@Test
	public void byDefaultOnlyRequiredAreResolved() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		assertTrue( resolver.isResolveRequired() );
		assertFalse( resolver.isResolveOptional() );
	}

	@Test
	public void withoutBasePackagesNoModulesAreResolved() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		resolver.setResolveOptional( true );
		resolver.setResolveRequired( true );

		assertFalse( resolver.resolveModule( ValidModule.NAME, true ).isPresent() );
		assertFalse( resolver.resolveModule( ValidModule.NAME, false ).isPresent() );
	}

	@Test
	public void resolveOnlyRequired() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		resolver.setBasePackages( "com.foreach.across.test.scan.packageOne" );
		resolver.setResolveOptional( false );
		resolver.setResolveRequired( true );

		assertTrue( resolver.resolveModule( ValidModule.NAME, true ).isPresent() );
		assertFalse( resolver.resolveModule( ValidModule.NAME, false ).isPresent() );
	}

	@Test
	public void resolveOnlyOptional() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		resolver.setBasePackages( "com.foreach.across.test.scan.packageOne" );
		resolver.setResolveOptional( true );
		resolver.setResolveRequired( false );

		assertFalse( resolver.resolveModule( ValidModule.NAME, true ).isPresent() );
		assertTrue( resolver.resolveModule( ValidModule.NAME, false ).isPresent() );
	}

	@Test
	public void resolveAll() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		resolver.setBasePackages( "com.foreach.across.test.scan.packageOne" );
		resolver.setResolveOptional( true );
		resolver.setResolveRequired( true );

		assertTrue( resolver.resolveModule( ValidModule.NAME, true ).isPresent() );
		assertTrue( resolver.resolveModule( ValidModule.NAME, false ).isPresent() );
	}

	@Test
	public void excludeModules() {
		ClassPathScanningModuleDependencyResolver resolver = new ClassPathScanningModuleDependencyResolver();
		resolver.setBasePackages( "com.foreach.across.test.scan.packageOne" );
		resolver.setResolveOptional( true );
		resolver.setResolveRequired( true );
		resolver.setExcludedModules( Collections.singleton( ValidModule.NAME ) );

		assertTrue( resolver.resolveModule( ExtendedValidModule.NAME, true ).isPresent() );
		assertTrue( resolver.resolveModule( ExtendedValidModule.NAME, false ).isPresent() );
		assertFalse( resolver.resolveModule( ValidModule.NAME, true ).isPresent() );
		assertFalse( resolver.resolveModule( ValidModule.NAME, false ).isPresent() );
	}
}
