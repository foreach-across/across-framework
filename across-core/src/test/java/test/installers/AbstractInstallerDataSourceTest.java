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
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = AbstractInstallerDataSourceTest.Config.class)
public abstract class AbstractInstallerDataSourceTest
{
	protected static final String MODULE_DS = "expectedModuleDataSource";
	protected static final String MODULE_INSTALLER_DS = "expectedModuleInstallerDataSource";

	private JdbcTemplate core, data;

	@Autowired
	private DataSource expectedModuleDataSource, expectedModuleInstallerDataSource;

	@Autowired
	private InstallerModuleBean installerModuleBean;

	@Autowired
	private void setAcrossDataSource( EmbeddedDatabase acrossDataSource ) {
		core = new JdbcTemplate( acrossDataSource );
	}

	@Autowired
	private void setDataDataSource( EmbeddedDatabase dataDataSource ) {
		data = new JdbcTemplate( dataDataSource );
	}

	@Test
	public void bootstrapLockShouldBeCreatedButNoLongerHeld() {
		assertEquals(
				Integer.valueOf( 1 ),
				core.queryForObject(
						"SELECT count(*) FROM " + coreSchema() + ".across_locks WHERE lock_id = 'across:bootstrap' AND holds = 0",
						Integer.class
				)
		);
	}

	@Test
	public void installerRecordShouldBeCreated() {
		assertEquals(
				Integer.valueOf( 1 ),
				core.queryForObject(
						"SELECT count(*) FROM " + coreSchema() + ".acrossmodules WHERE installer_id = '"
								+ MyInstaller.class.getName() + "'",
						Integer.class
				)
		);
	}

	@Test
	public void installerTableShouldBeCreatedInRightDataSource() {
		assertEquals(
				Integer.valueOf( 1 ),
				data.queryForObject(
						"SELECT count(*) FROM " + dataSchema() + ".my_installer WHERE name = 'test'",
						Integer.class
				)
		);
	}

	@Test
	public void verifyDataSources() {
		assertSame( expectedModuleDataSource, installerModuleBean.acrossDataSource );
		assertSame( expectedModuleInstallerDataSource, installerModuleBean.installerDataSource );
	}

	protected abstract String coreSchema();

	protected abstract String dataSchema();

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public InstallerModule installerModule() {
			return new InstallerModule();
		}
	}

	static class InstallerModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "InstallerModule";
		}

		@Override
		public String getDescription() {
			return "Only has a single installer.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { MyInstaller.class };
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( InstallerModuleBean.class ) );
		}
	}

	@Exposed
	static class InstallerModuleBean
	{
		public final DataSource acrossDataSource;
		public final DataSource installerDataSource;

		@Autowired
		public InstallerModuleBean( DataSource acrossDataSource, DataSource installerDataSource ) {
			this.acrossDataSource = acrossDataSource;
			this.installerDataSource = installerDataSource;
		}
	}

	@Installer(description = "Creates a simple table in the datasource.", version = 1)
	static class MyInstaller extends AcrossLiquibaseInstaller
	{
		public MyInstaller() {
			super( "classpath:/liquibase/my-installer.xml" );
		}
	}
}
