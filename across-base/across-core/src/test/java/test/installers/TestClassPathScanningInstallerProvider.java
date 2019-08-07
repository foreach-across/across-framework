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
package test.installers;

import com.foreach.across.core.context.installers.ClassPathScanningInstallerProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import test.installers.examples.InstallerThree;
import test.installers.scan.installers.InstallerOne;
import test.installers.scan.installers.InstallerTwo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningInstallerProvider
{
	private ClassPathScanningInstallerProvider provider = new ClassPathScanningInstallerProvider( new PathMatchingResourcePatternResolver() );

	@Test
	public void noInstallers() {
		Set<Class<?>> installers = provider.scan( "illegal" );

		assertNotNull( installers );
		assertTrue( installers.isEmpty() );
	}

	@Test
	public void singlePackage() {
		Set<Class<?>> installers = provider.scan( "test.installers.scan.installers" );

		assertNotNull( installers );
		assertEquals( 2, installers.size() );
		assertEquals(
				new HashSet<>( Arrays.asList( InstallerOne.class, InstallerTwo.class ) ),
				installers
		);
	}

	@Test
	public void multiPackagesAtOnce() {
		Set<Class<?>> installers = provider.scan(
				"test.installers.scan.installers",
				"test.installers.examples"
		);

		assertNotNull( installers );
		assertEquals( 3, installers.size() );
		assertEquals(
				new HashSet<>( Arrays.asList( InstallerOne.class, InstallerTwo.class, InstallerThree.class ) ),
				installers
		);
	}
}
