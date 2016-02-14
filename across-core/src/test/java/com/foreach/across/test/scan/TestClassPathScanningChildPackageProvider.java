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

import com.foreach.across.core.context.ClassPathScanningChildPackageProvider;
import com.foreach.across.test.dynamic.pkg.PkgMember;
import com.foreach.across.test.dynamic.pkg.config.PkgConfig;
import com.foreach.across.test.dynamic.pkg.controllers.PkgController;
import com.foreach.across.test.dynamic.pkg.extensions.PkgExtensionConfig;
import com.foreach.across.test.dynamic.pkg.installers.PkgInstaller;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningChildPackageProvider
{
	@Test
	public void noChildren() {
		ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider();
		String[] children = packageProvider.findChildren( "ksdjflksdjklsdjds" );

		assertNotNull( children );
		assertEquals( 0, children.length );
	}

	@Test
	public void noExcludes() {
		ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider();
		String[] children = packageProvider.findChildren( PkgMember.class.getPackage().getName() );

		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName(), PkgController.class.getPackage().getName(),
				               PkgExtensionConfig.class.getPackage().getName(),
				               PkgInstaller.class.getPackage().getName() },
				children
		);
	}

	@Test
	public void excludes() {
		ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider();
		packageProvider.setExcludedChildPackages( "installers", "extensions" );

		String[] children = packageProvider.findChildren( PkgMember.class.getPackage().getName() );

		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName(), PkgController.class.getPackage().getName() },
				children
		);
	}
}
