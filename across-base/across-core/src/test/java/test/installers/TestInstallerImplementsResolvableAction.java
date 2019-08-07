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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.installers.InstallerActionResolver;
import com.foreach.across.core.installers.InstallerMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestInstallerImplementsResolvableAction
{
	private static InstallerAction actionToResolve;
	private static boolean resolved, executed;

	@BeforeEach
	public void before() {
		actionToResolve = null;
		resolved = false;
		executed = false;
	}

	@Test
	public void executeWithoutAlternateAction() {
		Integer version = executeAndReturnVersion( InstallerAction.EXECUTE );

		assertEquals( Integer.valueOf( 1 ), version );
		assertTrue( resolved );
		assertTrue( executed );
	}

	@Test
	public void registerOnly() {
		actionToResolve = InstallerAction.REGISTER;
		Integer version = executeAndReturnVersion( InstallerAction.EXECUTE );

		assertEquals( Integer.valueOf( 1 ), version );
		assertTrue( resolved );
		assertFalse( executed );
	}

	@Test
	public void skip() {
		actionToResolve = InstallerAction.SKIP;
		Integer version = executeAndReturnVersion( InstallerAction.EXECUTE );

		assertNull( version );
		assertTrue( resolved );
		assertFalse( executed );
	}

	@Test
	public void forceSkipsResolving() {
		Integer version = executeAndReturnVersion( InstallerAction.FORCE );

		assertEquals( Integer.valueOf( 1 ), version );
		assertFalse( resolved );
		assertTrue( executed );
	}

	private Integer executeAndReturnVersion( InstallerAction defaultAction ) {
		EmbeddedDatabase ds = new EmbeddedDatabaseBuilder().setType( EmbeddedDatabaseType.HSQL ).build();

		AcrossContext ctx = new AcrossContext();
		ctx.setInstallerAction( defaultAction );
		ctx.setDataSource( ds );
		ctx.setInstallerAction( defaultAction );
		ctx.addModule( new MyModule() );
		ctx.bootstrap();

		Integer installedVersion = null;

		try {
			installedVersion = new JdbcTemplate( ds ).queryForObject(
					"SELECT version FROM acrossmodules WHERE installer_id = 'myInstaller'",
					Integer.class
			);
		}
		catch ( EmptyResultDataAccessException ignore ) {
		}

		ctx.shutdown();
		ds.shutdown();

		return installedVersion;
	}

	protected static class MyModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "MyModule";
		}

		@Override
		public String getDescription() {
			return "Custom installer action resolver";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { MyInstaller.class };
		}
	}

	@Installer(description = "resolverInstaller", name = "myInstaller")
	protected static class MyInstaller implements InstallerActionResolver
	{
		@Override
		public Optional<InstallerAction> resolve( String moduleName, InstallerMetaData installerMetaData ) {
			resolved = true;
			return Optional.ofNullable( actionToResolve );
		}

		@InstallerMethod
		public void install() {
			executed = true;
		}
	}
}
