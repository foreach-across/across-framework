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
package test.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.ClassPathScanningModuleDependencyResolver;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.support.AcrossContextBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestAcrossContextBuilder
{
	private AcrossContextBuilder builder;
	private AcrossContext context;

	@Before
	public void setUp() {
		builder = new AcrossContextBuilder();
		context = null;
	}

	@Test
	public void defaults() {
		build();

		assertNotNull( context );
		assertNull( context.getParentApplicationContext() );
		assertNull( context.getDataSource() );
		assertNull( context.getInstallerDataSource() );
		assertEquals( InstallerAction.DISABLED, context.getInstallerAction() );
		assertFalse( context.isDevelopmentMode() );
		assertNotNull( context.getModuleDependencyResolver() );

		ClassPathScanningModuleDependencyResolver dependencyResolver
				= (ClassPathScanningModuleDependencyResolver) context.getModuleDependencyResolver();
		assertTrue( dependencyResolver.isResolveRequired() );
		assertFalse( dependencyResolver.isResolveOptional() );
	}

	@Test
	public void applicationContext() throws IOException {
		ApplicationContext applicationContext = mock( ApplicationContext.class );
		builder.applicationContext( applicationContext );

		when( applicationContext.getResources( any() ) ).thenReturn( new Resource[0] );

		build();
		assertSame( applicationContext, context.getParentApplicationContext() );
	}

	@Test
	public void onlyDataSource() {
		DataSource ds = mock( DataSource.class );

		builder.dataSource( ds );
		build();

		assertSame( ds, context.getDataSource() );
		assertSame( ds, context.getInstallerDataSource() );
		assertEquals( InstallerAction.EXECUTE, context.getInstallerAction() );
	}

	@Test
	public void onlyInstallerDataSource() {
		DataSource ds = mock( DataSource.class );

		builder.installerDataSource( ds );
		build();

		assertNull( context.getDataSource() );
		assertSame( ds, context.getInstallerDataSource() );
		assertEquals( InstallerAction.EXECUTE, context.getInstallerAction() );
	}

	@Test
	public void bothDataSources() {
		DataSource ds = mock( DataSource.class );
		DataSource installerDs = mock( DataSource.class );

		builder.dataSource( ds ).installerDataSource( installerDs );
		build();

		assertSame( ds, context.getDataSource() );
		assertSame( installerDs, context.getInstallerDataSource() );
		assertEquals( InstallerAction.EXECUTE, context.getInstallerAction() );
	}

	@Test
	public void developmentMode() {
		builder.developmentMode( true );
		build();
		assertTrue( context.isDevelopmentMode() );
	}

	@Test
	public void moduleDependencyResolver() {
		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		builder.moduleDependencyResolver( dependencyResolver );

		build();

		assertSame( dependencyResolver, context.getModuleDependencyResolver() );
	}

	@Test
	public void moduleConfigurationPackages() {
		builder.moduleConfigurationPackages( "test", "one" );
		builder.moduleConfigurationPackageClasses( String.class );

		build();

		assertArrayEquals(
				new String[] { "test", "one", "java.lang" },
				context.getModuleConfigurationScanPackages()
		);
	}

	@Test
	public void configurers() {
		List<AcrossContextConfigurer> tracker = new ArrayList<>();
		Configurer one = new Configurer( tracker );
		Configurer two = new Configurer( tracker );
		Configurer three = new Configurer( tracker );

		builder.configurer( one ).configurer( one, two ).configurer( three );

		build();

		assertNotNull( context );
		assertEquals( 3, tracker.size() );
		assertSame( one, tracker.get( 0 ) );
		assertSame( two, tracker.get( 1 ) );
		assertSame( three, tracker.get( 2 ) );

		assertSame( context, one.getContext() );
		assertSame( context, two.getContext() );
		assertSame( context, three.getContext() );
	}

	@Test
	public void modules() {
		AcrossModule one = new EmptyAcrossModule( "one" );
		AcrossModule two = new EmptyAcrossModule( "two" );

		ModuleDependencyResolver dependencyResolver = mock( ModuleDependencyResolver.class );
		when( dependencyResolver.resolveModule( "one", true ) ).thenReturn( Optional.of( one ) );

		builder.moduleDependencyResolver( dependencyResolver )
		       .modules( "one" )
		       .modules( two );

		build();
		assertNotNull( context );
		assertEquals( 2, context.getModules().size() );
		assertTrue( context.getModules().contains( one ) );
		assertTrue( context.getModules().contains( two ) );
	}

	private void build() {
		context = builder.build();
	}

	private static class Configurer implements AcrossContextConfigurer
	{
		private final List<AcrossContextConfigurer> tracker;
		private AcrossContext context;

		public Configurer( List<AcrossContextConfigurer> tracker ) {
			this.tracker = tracker;
		}

		public AcrossContext getContext() {
			return context;
		}

		@Override
		public void configure( AcrossContext context ) {
			tracker.add( this );
			this.context = context;
		}
	}

}
