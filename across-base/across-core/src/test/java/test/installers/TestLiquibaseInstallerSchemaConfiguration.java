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
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import com.foreach.across.core.installers.InstallerAction;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by adbw on 29/01/2016.
 */
public class TestLiquibaseInstallerSchemaConfiguration
{
	@Test
	public void defaultSchema() {
		assertInstalledInSchema( "PUBLIC" );
	}

	@Test
	public void singleDefaultSchemaConfiguration() {
		assertInstalledInSchema( "TEST_SCHEMA", DefaultSchemaConfiguration.class );
	}

	@Test
	public void moduleSpecificSchemaConfiguration() {
		assertInstalledInSchema(
				"MY_MODULE_SCHEMA", DefaultSchemaConfiguration.class, MyModuleSchemaConfiguration.class
		);
	}

	@Test
	public void otherModuleSchemaConfiguration() {
		assertInstalledInSchema( "PUBLIC", OtherModuleSchemaConfiguration.class );
	}

	private void assertInstalledInSchema( String expectedSchema, Class<?>... annotatedClasses ) {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
		if ( annotatedClasses.length > 0 ) {
			parent.register( annotatedClasses );
		}
		parent.refresh();
		parent.start();

		EmbeddedDatabase ds = new EmbeddedDatabaseBuilder()
				.setType( EmbeddedDatabaseType.HSQL )
				.addScript( "classpath:/liquibase/hsqldb-create-test_schema.sql" )
				.addScript( "classpath:/liquibase/hsqldb-create-my_module_schema.sql" )
				.build();

		AcrossContext ctx = new AcrossContext();
		ctx.setParentApplicationContext( parent );
		ctx.setInstallerAction( InstallerAction.EXECUTE );
		ctx.setDataSource( ds );
		ctx.addModule( new MyModule() );
		ctx.bootstrap();

		assertEquals(
				Integer.valueOf( 1 ),
				new JdbcTemplate( ds )
						.queryForObject( "SELECT count(*) FROM " + expectedSchema + ".my_installer", Integer.class )
		);

		ctx.shutdown();
		parent.close();
		ds.shutdown();
	}

	@Configuration
	protected static class DefaultSchemaConfiguration
	{
		@Bean
		public SchemaConfiguration schemaConfiguration() {
			return new SchemaConfiguration( "TEST_SCHEMA" );
		}
	}

	@Configuration
	protected static class MyModuleSchemaConfiguration
	{
		@Bean
		@Module("MyModule")
		public SchemaConfiguration schemaConfiguration() {
			return new SchemaConfiguration( "MY_MODULE_SCHEMA" );
		}
	}

	@Configuration
	protected static class OtherModuleSchemaConfiguration
	{
		@Bean
		@Module("OtherModule")
		public SchemaConfiguration schemaConfiguration() {
			return new SchemaConfiguration( "BLABLABLA" );
		}
	}

	protected static class MyModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "MyModule";
		}

		@Override
		public String getDescription() {
			return "Simple liquibase installer";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { MyInstaller.class };
		}
	}

	@Installer(description = "Creates a simple table in the datasource.", version = 1)
	protected static class MyInstaller extends AcrossLiquibaseInstaller
	{
		public MyInstaller() {
			super( "classpath:/liquibase/my-installer.xml" );
		}
	}
}
