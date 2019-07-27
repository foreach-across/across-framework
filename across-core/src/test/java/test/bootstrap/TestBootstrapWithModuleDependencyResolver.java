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
package test.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
public class TestBootstrapWithModuleDependencyResolver
{
	@Test
	public void dependenciesShouldBeResolved() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( "ModuleTwo", true ) ).thenReturn( Optional.of( new ModuleTwo() ) );
		when( dependencyResolver.resolveModule( "ModuleThree", false ) ).thenReturn( Optional.of( new ModuleThree() ) );

		AcrossContext context = new AcrossContext();
		context.setModuleDependencyResolver( dependencyResolver );
		context.addModule( new ModuleOne() );
		context.bootstrap();

		AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo( context );
		assertEquals( 3, contextInfo.getModules().size() );

		assertTrue( contextInfo.hasModule( "ModuleOne" ) );
		assertTrue( contextInfo.hasModule( "ModuleTwo" ) );
		assertTrue( contextInfo.hasModule( "ModuleThree" ) );

		context.shutdown();
	}

	@AcrossDepends(required = "ModuleTwo")
	static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}

		@Override
		public String getDescription() {
			return null;
		}
	}

	@AcrossDepends(optional = "ModuleThree")
	static class ModuleTwo extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	static class ModuleThree extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}
}
