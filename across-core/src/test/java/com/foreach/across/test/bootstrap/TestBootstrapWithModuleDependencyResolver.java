package com.foreach.across.test.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
