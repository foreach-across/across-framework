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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.ClassPathScanningCandidateModuleProvider;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageOne.ValidModule;
import test.scan.packageTwo.OtherValidModule;
import test.scan.packageTwo.ReplacementValidModule;
import test.scan.packageTwo.YetAnotherValidModule;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import test.scan.packageOne.ExtendedValidModule;
import test.scan.packageOne.ValidModule;
import test.scan.packageTwo.OtherValidModule;
import test.scan.packageTwo.ReplacementValidModule;
import test.scan.packageTwo.YetAnotherValidModule;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningCandidateModuleProvider
{
	private ClassPathScanningCandidateModuleProvider moduleProvider = new ClassPathScanningCandidateModuleProvider( new PathMatchingResourcePatternResolver() );

	@Test
	public void noModules() {
		Map<String, Supplier<AcrossModule>> candidates
				= moduleProvider.findCandidateModules( "illegal" );

		assertNotNull( candidates );
		assertTrue( candidates.isEmpty() );
	}

	@Test
	public void modulesFromPackageOne() {
		Map<String, Supplier<AcrossModule>> candidates
				= moduleProvider.findCandidateModules( "test.scan.packageOne" );

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
		Map<String, Supplier<AcrossModule>> candidates = moduleProvider.findCandidateModules(
				"test.scan.packageOne",
				"test.scan.packageTwo"
		);

		assertNotNull( candidates );
		assertEquals( 4, candidates.size() );

		assertTrue( candidates.containsKey( ExtendedValidModule.NAME ) );
		assertTrue( candidates.containsKey( OtherValidModule.NAME ) );
		assertTrue( candidates.containsKey( YetAnotherValidModule.NAME ) );

		// module should be the version of packageTwo
		Supplier<AcrossModule> supplier = candidates.get( ValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ReplacementValidModule.class.equals( supplier.get().getClass() ) );
	}

	@Test
	public void packageOneOverridesPackageTwo() {
		Map<String, Supplier<AcrossModule>> candidates = moduleProvider.findCandidateModules(
				"test.scan.packageTwo",
				"test.scan.packageOne"
		);

		assertNotNull( candidates );
		assertEquals( 4, candidates.size() );

		assertTrue( candidates.containsKey( ExtendedValidModule.NAME ) );
		assertTrue( candidates.containsKey( OtherValidModule.NAME ) );
		assertTrue( candidates.containsKey( YetAnotherValidModule.NAME ) );

		// module should be the version of packageOne
		Supplier<AcrossModule> supplier = candidates.get( ValidModule.NAME );
		assertNotNull( supplier );
		assertTrue( ValidModule.class.equals( supplier.get().getClass() ) );
	}
}
