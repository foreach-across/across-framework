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

import com.foreach.across.core.context.ClassPathScanningChildPackageProvider;
import test.dynamic.pkg.PkgMember;
import test.dynamic.pkg.config.PkgConfig;
import test.dynamic.pkg.controllers.PkgController;
import test.dynamic.pkg.extensions.PkgExtensionConfig;
import test.dynamic.pkg.installers.PkgInstaller;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import test.dynamic.pkg.PkgMember;
import test.dynamic.pkg.config.PkgConfig;
import test.dynamic.pkg.controllers.PkgController;
import test.dynamic.pkg.extensions.PkgExtensionConfig;
import test.dynamic.pkg.installers.PkgInstaller;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestClassPathScanningChildPackageProvider
{
	private ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider( new PathMatchingResourcePatternResolver() );

	@Test
	public void noChildren() {
		String[] children = packageProvider.findChildren( "ksdjflksdjklsdjds" );

		assertNotNull( children );
		assertEquals( 0, children.length );
	}

	@Test
	public void noExcludes() {
		String[] children = packageProvider.findChildren( PkgMember.class.getPackage().getName() );

		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName(), PkgController.class.getPackage().getName(),
				               PkgExtensionConfig.class.getPackage().getName(),
				               PkgInstaller.class.getPackage().getName(),
				               PkgMember.class.getPackage().getName() + ".util"
				},
				children
		);
	}

	@Test
	public void excludes() {
		packageProvider.setExcludedChildPackages( "installers", "extensions" );

		String[] children = packageProvider.findChildren( PkgMember.class.getPackage().getName() );

		assertArrayEquals(
				new String[] { PkgConfig.class.getPackage().getName(), PkgController.class.getPackage().getName(),
				               PkgMember.class.getPackage().getName() + ".util" },
				children
		);
	}
}
