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
package com.foreach.across.test.support;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class TestAcrossTestContextBuilder
{
	@Test
	public void closeable() {
		AcrossContext acrossContext;

		try (AcrossTestContext testContext = contextBuilder().build()) {
			acrossContext = testContext.contextInfo().getContext();
			assertTrue( acrossContext.isBootstrapped() );
		}

		assertNotNull( acrossContext );
		assertFalse( acrossContext.isBootstrapped() );
	}

	@Test
	public void defaultDataSourceIsCreated() {
		try (AcrossTestContext context = contextBuilder().dropFirst( false ).build()) {
			DataSource dataSource = context.getBean( AcrossContext.DATASOURCE, DataSource.class );
			DataSource installerDataSource = context.getBean( AcrossContext.INSTALLER_DATASOURCE, DataSource.class );

			assertNotNull( dataSource );
			assertNotNull( installerDataSource );
			assertSame( dataSource, installerDataSource );
		}
	}

	@Test
	public void manualDataSources() {
		DataSource ds = mock( DataSource.class );
		DataSource installerDs = mock( DataSource.class );

		try (
				AcrossTestContext context = contextBuilder()
						.dropFirst( false )
						.dataSource( ds )
						.installerDataSource( installerDs ).build()
		) {
			DataSource dataSource = context.getBean( AcrossContext.DATASOURCE, DataSource.class );
			DataSource installerDataSource = context.getBean( AcrossContext.INSTALLER_DATASOURCE, DataSource.class );

			assertSame( ds, dataSource );
			assertSame( installerDs, installerDataSource );
		}
	}

	@Test
	public void noDefaultDataSources() {
		try (AcrossTestContext context = contextBuilder().useTestDataSource( false ).build()) {
			DataSource dataSource = context.getBean( AcrossContext.DATASOURCE, DataSource.class );
			DataSource installerDataSource = context.getBean( AcrossContext.INSTALLER_DATASOURCE, DataSource.class );

			assertNull( dataSource );
			assertNull( installerDataSource );
		}
	}

	@Test
	public void dataSourceContentShouldBeDropped() {
		DataSource ds = testDataSource();
		DataSource installerDs = testDataSource();

		assertTestQueryOk( ds );
		assertTestQueryOk( installerDs );

		try (
				AcrossTestContext ignore = contextBuilder()
						.useTestDataSource( false )
						.dataSource( ds )
						.installerDataSource( installerDs )
						.build()
		) {
			assertTestQueryFails( ds );
			assertTestQueryFails( installerDs );
		}
	}

	@Test
	public void dataSourceContentShouldNotBeDropped() {
		DataSource ds = testDataSource();
		DataSource installerDs = testDataSource();

		assertTestQueryOk( ds );
		assertTestQueryOk( installerDs );

		try (
				AcrossTestContext ignore = contextBuilder()
						.useTestDataSource( false )
						.dropFirst( false )
						.dataSource( ds )
						.installerDataSource( installerDs )
						.build()
		) {
			assertTestQueryOk( ds );
			assertTestQueryOk( installerDs );
		}
	}

	@Test
	public void manualExposingOfItems() {
		try (
				AcrossTestContext ctx = contextBuilder()
						.modules( new EmptyAcrossModule( "testModule", ModuleConfig.class ) )
						.useTestDataSource( false )
						.build()
		) {
			assertFalse( ctx.containsBean( "testModuleConfig" ) );
		}

		try (
				AcrossTestContext ctx = contextBuilder()
						.expose( Configuration.class )
						.modules( new EmptyAcrossModule( "testModule", ModuleConfig.class ) )
						.useTestDataSource( false )
						.build()
		) {
			assertTrue( ctx.containsBean( "testModuleConfig" ) );
		}
	}

	@Configuration("testModuleConfig")
	static class ModuleConfig
	{

	}

	protected AcrossTestContextBuilder contextBuilder() {
		return new AcrossTestContextBuilder();
	}

	private DataSource testDataSource() {
		return new EmbeddedDatabaseBuilder()
				.setName( UUID.randomUUID().toString() )
				.addScript( "scripts/create_test_table.sql" )
				.build();
	}

	private void assertTestQueryOk( DataSource dataSource ) {
		try {
			new JdbcTemplate( dataSource ).execute( "select * from test" );
		}
		catch ( Exception ignore ) {
			fail( "Query did not execute ok" );
		}
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
}
