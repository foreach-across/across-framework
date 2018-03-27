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
package test.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import test.TestAcrossContext;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestComponentScanConfigurer
{
	@Test
	public void packagesAsString() {
		ComponentScanConfigurer configurer
				= new ComponentScanConfigurer( "test", "com.foreach.across" );

		assertArrayEquals(
				new String[] { "test", "com.foreach.across" },
				configurer.componentScanPackages()
		);

		assertNoOtherProperties( configurer );
	}

	@Test
	public void packagesAsClass() {
		ComponentScanConfigurer configurer
				= new ComponentScanConfigurer( TestAcrossContext.class, AcrossContext.class );

		assertArrayEquals(
				new String[] { "test", "com.foreach.across.core" },
				configurer.componentScanPackages()
		);

		assertNoOtherProperties( configurer );
	}

	private void assertNoOtherProperties( ComponentScanConfigurer configurer ) {
		assertEquals( 0, configurer.annotatedClasses().length );
		assertEquals( 0, configurer.postProcessors().length );
		assertNull( configurer.providedBeans() );
		assertNull( configurer.propertySources() );
	}
}
