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
package com.foreach.across.test.support;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration
public class TestAcrossTestConfiguration
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Autowired(required = false)
	private MyManuallyExposedComponent manuallyExposedComponent;

	@Test
	void dataSourceForTestShouldBeCreated() {
		AcrossContext ctx = contextInfo.getContext();
		assertNotNull( ctx.getDataSource() );
		assertNotNull( ctx.getInstallerDataSource() );
		assertNotSame( ctx.getDataSource(), ctx.getInstallerDataSource() );
	}

	@Test
	void installerDataSourceShouldBeReset() {
		assertTestQueryFails( contextInfo.getContext().getInstallerDataSource() );
	}

	@Test
	void modulesShouldBePresent() {
		assertEquals( 3, contextInfo.getModules().size() );
		assertEquals( 4, contextInfo.getConfiguredModules().size() );
		assertFalse( contextInfo.getModuleInfo( AcrossBootstrapConfigurer.CONTEXT_INFRASTRUCTURE_MODULE ).isBootstrapped() );
		assertTrue( contextInfo.hasModule( AcrossWebModule.NAME ) );
		assertTrue( contextInfo.hasModule( "named" ) );
		assertTrue( contextInfo.hasModule( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE ) );
		assertTrue( contextInfo.hasModule( AcrossBootstrapConfigurer.CONTEXT_INFRASTRUCTURE_MODULE ) );
	}

	@Test
	void manuallyExposedComponentShouldActuallyBeExposed() {
		assertNotNull( manuallyExposedComponent );
	}

	private void assertTestQueryFails( DataSource dataSource ) {
		try {
			new JdbcTemplate( dataSource ).execute( "select * from test" );
		}
		catch ( Exception ignore ) {
			return;
		}
		fail( "Query executed but should not have" );
	}

	@AcrossTestConfiguration(modules = { AcrossWebModule.NAME }, expose = MyManuallyExposedComponent.class)
	protected static class Config
	{
		@Bean
		public DataSource acrossInstallerDataSource() {
			DataSource installerDs = new EmbeddedDatabaseBuilder()
					.setName( UUID.randomUUID().toString() )
					.addScript( "scripts/create_test_table.sql" )
					.build();
			assertTestQueryOk( installerDs );
			return installerDs;
		}

		@Bean
		public AcrossModule namedModule() {
			return new EmptyAcrossModule( "named", MyManuallyExposedComponent.class );
		}

		private void assertTestQueryOk( DataSource dataSource ) {
			try {
				new JdbcTemplate( dataSource ).execute( "select * from test" );
			}
			catch ( Exception ignore ) {
				fail( "Query did not execute ok" );
			}
		}
	}

	@Component
	static class MyManuallyExposedComponent
	{
	}
}
