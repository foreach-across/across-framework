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
package com.foreach.across.test.application;

import com.foreach.across.config.AcrossDynamicModulesConfigurer;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.AcrossTestWebContext;
import com.foreach.across.test.application.app.DummyApplication;
import org.junit.jupiter.api.Test;

import static com.foreach.across.test.support.AcrossTestBuilders.standard;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestDynamicModulesInBuilders
{
	@Test
	public void nonWebBuilder() {
		try (
				AcrossTestContext ctx = standard( false )
						.configurer( new AcrossDynamicModulesConfigurer( DummyApplication.class ) )
						.build()
		) {
			assertTrue( ctx.contextInfo().hasModule( "DummyApplicationModule" ) );
			assertTrue( ctx.contextInfo().hasModule( "DummyInfrastructureModule" ) );
			assertFalse( ctx.contextInfo().hasModule( "DummyPostProcessorModule" ) );
		}
	}

	@Test
	public void webBuilder() {
		String pkg = DummyApplication.class.getPackage().getName();

		try (
				AcrossTestWebContext ctx = web( false )
						.configurer( new AcrossDynamicModulesConfigurer( pkg, "Test" ) )
						.build()
		) {
			assertTrue( ctx.contextInfo().hasModule( "TestApplicationModule" ) );
			assertTrue( ctx.contextInfo().hasModule( "TestInfrastructureModule" ) );
			assertFalse( ctx.contextInfo().hasModule( "TestPostProcessorModule" ) );
		}
	}
}
