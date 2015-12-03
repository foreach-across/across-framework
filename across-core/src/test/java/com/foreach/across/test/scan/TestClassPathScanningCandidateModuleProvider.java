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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ClassPathScanningCandidateModuleProvider;
import com.foreach.across.test.scan.packageOne.ExtendedValidModule;
import com.foreach.across.test.scan.packageOne.ValidModule;
import com.foreach.across.test.scan.packageTwo.OtherValidModule;
import com.foreach.across.test.scan.packageTwo.ReplacementValidModule;
import org.junit.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningCandidateModuleProvider
{
	@Test
	public void noModules() {
		ClassPathScanningCandidateModuleProvider moduleProvider = new ClassPathScanningCandidateModuleProvider();
		Map<String, Supplier<AcrossModule>> candidates
				= moduleProvider.findCandidateModules( "illegal" );

		assertNotNull( candidates );
		assertTrue( candidates.isEmpty() );
	}

	@Test
	public void modulesFromPackageOne() {
		ClassPathScanningCandidateModuleProvider moduleProvider = new ClassPathScanningCandidateModuleProvider();
		Map<String, Supplier<AcrossModule>> candidates
				= moduleProvider.findCandidateModules( "com.foreach.across.test.scan.packageOne" );

		assertNotNull( candidates );
		assertEquals( 2, candidates.size() );

		Supplier<AcrossModule> supplier = candidates.get( ValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ValidModule.class.equals( supplier.get().getClass() ) );

		supplier = candidates.get( ExtendedValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ExtendedValidModule.class.equals( supplier.get().getClass() ) );
	}

	@Test
	public void packageTwoOverridesPackageOne() {
		ClassPathScanningCandidateModuleProvider moduleProvider = new ClassPathScanningCandidateModuleProvider();
		Map<String, Supplier<AcrossModule>> candidates = moduleProvider.findCandidateModules(
				"com.foreach.across.test.scan.packageOne",
				"com.foreach.across.test.scan.packageTwo"
		);

		assertNotNull( candidates );
		assertEquals( 3, candidates.size() );

		assertTrue( candidates.containsKey( ExtendedValidModule.NAME ) );
		assertTrue( candidates.containsKey( OtherValidModule.NAME ) );

		// module should be the version of packageTwo
		Supplier<AcrossModule> supplier = candidates.get( ValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ReplacementValidModule.class.equals( supplier.get().getClass() ) );
	}

	@Test
	public void packageOneOverridesPackageTwo() {
		ClassPathScanningCandidateModuleProvider moduleProvider = new ClassPathScanningCandidateModuleProvider();
		Map<String, Supplier<AcrossModule>> candidates = moduleProvider.findCandidateModules(
				"com.foreach.across.test.scan.packageTwo",
				"com.foreach.across.test.scan.packageOne"
		);

		assertNotNull( candidates );
		assertEquals( 3, candidates.size() );

		assertTrue( candidates.containsKey( ExtendedValidModule.NAME ) );
		assertTrue( candidates.containsKey( OtherValidModule.NAME ) );

		// module should be the version of packageOne
		Supplier<AcrossModule> supplier = candidates.get( ValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ValidModule.class.equals( supplier.get().getClass() ) );
	}
}
