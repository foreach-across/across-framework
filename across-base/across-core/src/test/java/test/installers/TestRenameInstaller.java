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
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import com.foreach.across.core.installers.InstallerRunCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestRenameInstaller.Config.class)
public class TestRenameInstaller
{
	private AcrossInstallerRepository installerRepository;

	@Autowired
	public void setAcrossContextBeanRegistry( AcrossContextBeanRegistry beanRegistry ) {
		installerRepository = beanRegistry.getBeanOfType( AcrossInstallerRepository.class );
	}

	@Test
	public void bothInstallersShouldHaveExecuted() {
		assertEquals( 1, installerRepository.getInstalledVersion( "MyModule", RenameInstaller.class.getName() ) );
		assertEquals( 2, installerRepository.getInstalledVersion( "MyModule", "installerOne" ) );
		assertEquals( -1, installerRepository.getInstalledVersion( "MyModule", MyInstaller.class.getName() ) );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config
	{
		@Bean
		public MyModule myModule() {
			return new MyModule();
		}

		@Bean
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.build();
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
			return "Custom installer action resolver";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] { MyInstaller.class, RenameInstaller.class, InstallerOne.class };
		}
	}

	@Installer(description = "Installer one", version = 1)
	protected static class MyInstaller
	{
		@InstallerMethod
		public void install() {
		}
	}

	@Installer(description = "Rename installers", runCondition = InstallerRunCondition.AlwaysRun)
	protected static class RenameInstaller
	{
		@Autowired
		private AcrossInstallerRepository installerRepository;

		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModuleInfo currentModule;

		@InstallerMethod
		public void install() {
			installerRepository.renameInstallerForModule(
					MyInstaller.class.getName(), "installerOne", currentModule.getName()
			);
		}
	}

	@Installer(description = "Installer two", version = 2, name = "installerOne")
	protected static class InstallerOne
	{
		@InstallerMethod
		public void install() {
		}
	}

}
