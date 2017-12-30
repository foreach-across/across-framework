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
package com.foreach.across.test.modules.web.support;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.across.modules.web.context.WebBootstrapApplicationContextFactory;
import com.foreach.across.modules.web.support.ApplicationContextIdNameGenerator;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestApplicationContextIdNameGenerator
{
	@Test
	public void moduleNamePrefixedByIndex() throws Exception {
		assertEquals( "[AX_02] Primary", ApplicationContextIdNameGenerator.forModule( module( "Primary", 2 ) ) );
		assertEquals( "[AX_69] Pink Module", ApplicationContextIdNameGenerator.forModule( module( "Pink Module", 69 ) ) );
	}

	private ModuleBootstrapConfig module( String moduleName, int bootstrapIndex ) {
		return new ModuleBootstrapConfig( new ConfigurableAcrossModuleInfo( null, new EmptyAcrossModule( moduleName ), bootstrapIndex ) );
	}

	@Test
	public void acrossContextNaming() throws Exception {
		AcrossContext acrossContext = new AcrossContext();
		acrossContext.bootstrap();
		ApplicationContext applicationContext = new WebBootstrapApplicationContextFactory().createApplicationContext( acrossContext, null );
		assertTrue( ApplicationContextIdNameGenerator.forContext( applicationContext ).startsWith( "[AX] AcrossContext-" ) );
	}

	@Test
	public void rootApplicationContextNaming() throws Exception {
		assertEquals( "[AX] # Root WebApplicationContext", ApplicationContextIdNameGenerator.forContext( new AcrossWebApplicationContext() ) );
	}
}
