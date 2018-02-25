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

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.common.concurrent.locks.distributed.DistributedLockException;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestCoreSchemaInstallerDataSource.Config.class)
public class TestCoreSchemaInstallerDataSource
{
	@Autowired
	@Qualifier(AcrossContext.INSTALLER_DATASOURCE)
	private EmbeddedDatabase installerDataSource;

	private AcrossInstallerRepository installerRepository;
	private DistributedLockRepository lockRepository;

	@Autowired
	public void setBeanRegistry( AcrossContextBeanRegistry beanRegistry ) {
		// Fetching the installer repository will install the core schema
		installerRepository = beanRegistry.getBeanOfType( AcrossInstallerRepository.class );
		lockRepository = beanRegistry.getBeanOfType( DistributedLockRepository.class );
	}

	@Test
	public void coreSchemaShouldBeInInstallerDataSource() {
		assertEquals(
				Integer.valueOf( 1 ),
				new JdbcTemplate( installerDataSource ).queryForObject(
						"SELECT count(*) FROM acrossmodules",
						Integer.class
				)

		);

		assertEquals(
				Integer.valueOf( 1 ),
				new JdbcTemplate( installerDataSource ).queryForObject(
						"SELECT count(*) FROM acrossmodules WHERE module_id = 'Across' AND installer_id = 'AcrossCoreSchemaInstaller'",
						Integer.class
				)

		);
	}

	@Test(expected = BadSqlGrammarException.class)
	public void usingInstallerRepositoryShouldFailBecauseSchemaInOtherDatasource() {
		installerRepository.getInstalledVersion( "nothing", "nowhere" );
	}

	@Test(expected = DistributedLockException.class)
	public void usingDistributedLockShouldFailBecauseSchemaInOtherDataSource() {
		lockRepository.lock( "someLock" ).unlock();
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config
	{
		@Bean(name = AcrossContext.DATASOURCE)
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.build();
		}

		@Bean(name = AcrossContext.INSTALLER_DATASOURCE)
		public EmbeddedDatabase installerDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "installer" )
					.build();
		}
	}
}
