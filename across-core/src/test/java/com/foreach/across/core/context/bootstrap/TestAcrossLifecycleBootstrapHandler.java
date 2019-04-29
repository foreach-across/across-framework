/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestAcrossLifecycleBootstrapHandler
{
	@Mock
	private AcrossBootstrapInfrastructure infrastructure;

	@Test
	void emptyContext() {
		AcrossContext empty = new AcrossContext();
		bootstrap( empty );
	}

	@Test
	void singleModule() {
		AcrossContext ctx = new AcrossContext();
		ctx.addModule( new EmptyAcrossModule( "SingleModule" ) );
		fail( "deliberate failure for build" );
		bootstrap( ctx );
	}

	@Test
	void extensionOnPostProcessor() {
		AcrossContext ctx = new AcrossContext();
		ctx.addModule( new ExtensionModule() );

		bootstrap( ctx );
	}

	@Test
	void singleModuleWithExtension() {
		AcrossContext ctx = new AcrossContext();
		ctx.addModule( new EmptyAcrossModule( "SingleModule" ) );
		ctx.addModule( new ExtensionModule() );

		bootstrap( ctx );
	}

	private void bootstrap( AcrossContext acrossContext ) {
		new AcrossLifecycleBootstrapHandler( acrossContext, infrastructure ).bootstrap();
	}

	private class ExtensionModule extends AcrossModule
	{

		@Override
		public String getName() {
			return "ExtensionModule";
		}

		@Override
		public String[] getExtensionTargets() {
			return new String[] { "SingleModule", AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE };
		}
	}

}
