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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestMultipleDataSourceInstallerOnly.Config.class)
public class TestMultipleDataSourceInstallerOnly extends AbstractInstallerDataSourceTest
{
	@Override
	protected String coreSchema() {
		return "PUBLIC";
	}

	@Override
	protected String dataSchema() {
		return "PUBLIC";
	}

	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Bean(name = { "acrossDataSource", MODULE_DS, MODULE_INSTALLER_DS })
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.build();
		}

		@Bean
		public EmbeddedDatabase dataDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "data" )
					.build();
		}

		@Override
		public void configure( AcrossContext context ) {
			context.getModule( "InstallerModule" )
			       .addInstallerContextConfigurer(
					       new SingletonBeanConfigurer( AcrossContext.INSTALLER_DATASOURCE, dataDataSource() )
			       );
		}
	}
}
