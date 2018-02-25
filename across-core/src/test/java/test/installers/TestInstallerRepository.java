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

import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.AcrossInstallerRepositoryImpl;
import com.foreach.across.core.installers.InstallerMetaData;
import test.installers.scan.installers.InstallerOne;
import test.installers.scan.installers.InstallerTwo;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.installers.scan.installers.InstallerOne;
import test.installers.scan.installers.InstallerTwo;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestInstallerRepository.Config.class)
public class TestInstallerRepository
{
	@Autowired
	private AcrossInstallerRepository installerRepository;

	@Test
	public void versionReturnedIfNotRegistered() {
		assertEquals( -1, installerRepository.getInstalledVersion( "VersionModule", "unknown" ) );
	}

	@Test
	public void installAndAssertVersion() {
		String moduleName = RandomStringUtils.random( 10 );

		InstallerMetaData installerOne = InstallerMetaData.forClass( InstallerOne.class );
		assertEquals( -1, installerRepository.getInstalledVersion( moduleName, installerOne.getName() ) );

		installerRepository.setInstalled( moduleName, installerOne );

		assertEquals( 1, installerRepository.getInstalledVersion( moduleName, installerOne.getName() ) );
	}

	@Test
	public void renameUnknownInstallerForModule() {
		assertFalse(
				installerRepository.renameInstallerForModule(
						RandomStringUtils.random( 10 ), RandomStringUtils.random( 10 ), RandomStringUtils.random( 10 )
				)
		);
	}

	@Test
	public void renameSingleModuleInstaller() {
		String moduleOne = RandomStringUtils.random( 11 );
		String moduleTwo = RandomStringUtils.random( 12 );

		InstallerMetaData installerOne = InstallerMetaData.forClass( InstallerOne.class );
		installerRepository.setInstalled( moduleOne, installerOne );
		installerRepository.setInstalled( moduleTwo, installerOne );

		assertEquals( 1, installerRepository.getInstalledVersion( moduleOne, installerOne.getName() ) );
		assertEquals( 1, installerRepository.getInstalledVersion( moduleTwo, installerOne.getName() ) );
		assertEquals( -1, installerRepository.getInstalledVersion( moduleTwo, "RenamedInstaller" ) );

		assertTrue(
				installerRepository.renameInstallerForModule( installerOne.getName(), "RenamedInstaller", moduleTwo )
		);

		assertEquals( 1, installerRepository.getInstalledVersion( moduleOne, installerOne.getName() ) );
		assertEquals( -1, installerRepository.getInstalledVersion( moduleTwo, installerOne.getName() ) );
		assertEquals( 1, installerRepository.getInstalledVersion( moduleTwo, "RenamedInstaller" ) );
	}

	@Test
	public void renameUnknownInstaller() {
		assertEquals(
				0,
				installerRepository.renameInstaller( RandomStringUtils.random( 15 ), RandomStringUtils.random( 15 ) )
		);
	}

	@Test
	public void renameInstaller() {
		String moduleOne = RandomStringUtils.random( 11 );
		String moduleTwo = RandomStringUtils.random( 12 );

		InstallerMetaData installerTwo = InstallerMetaData.forClass( InstallerTwo.class );
		installerRepository.setInstalled( moduleOne, installerTwo );
		installerRepository.setInstalled( moduleTwo, installerTwo );

		assertEquals( 1, installerRepository.getInstalledVersion( moduleOne, installerTwo.getName() ) );
		assertEquals( 1, installerRepository.getInstalledVersion( moduleTwo, installerTwo.getName() ) );
		assertEquals( -1, installerRepository.getInstalledVersion( moduleTwo, "RenamedInstaller2" ) );

		assertEquals(
				2,
				installerRepository.renameInstaller( installerTwo.getName(), "RenamedInstaller2" )
		);

		assertEquals( -1, installerRepository.getInstalledVersion( moduleOne, installerTwo.getName() ) );
		assertEquals( -1, installerRepository.getInstalledVersion( moduleTwo, installerTwo.getName() ) );
		assertEquals( 1, installerRepository.getInstalledVersion( moduleOne, "RenamedInstaller2" ) );
		assertEquals( 1, installerRepository.getInstalledVersion( moduleTwo, "RenamedInstaller2" ) );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		@DependsOn("springLiquibase")
		public AcrossInstallerRepository installerRepository() {
			return new AcrossInstallerRepositoryImpl( database() );
		}

		@Bean
		public EmbeddedDatabase database() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.build();
		}

		@Bean
		public SpringLiquibase springLiquibase() {
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource( database() );
			liquibase.setChangeLog( "classpath:com/foreach/across/core/installers/AcrossCoreSchemaInstaller.xml" );

			return liquibase;
		}
	}
}
