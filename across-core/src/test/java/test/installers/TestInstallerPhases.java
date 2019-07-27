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
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.installers.InstallerPhase;
import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestInstallerPhases.Config.class)
public class TestInstallerPhases
{
	private static List<Class> installersInOrder = new ArrayList<>();

	@Test
	public void installerOrder() {
		assertEquals(
				Arrays.asList( BeforeContextBootstrapInstaller.class, BeforeModuleBootstrapInstaller.class,
				               AfterModuleBootstrapInstaller.class, AfterContextBootstrapInstaller.class ),
				installersInOrder
		);
	}

	@Test
	public void beforeContextNoModuleShouldBeBootstrappedAndParentBeanShouldBeAvailable() {
		assertFalse( BeforeContextBootstrapInstaller.moduleIsBusyBeingBootstrapped );
		assertFalse( BeforeContextBootstrapInstaller.dummyModuleBootstrapped );
	}

	@Test
	public void beforeModuleBootstrappedPreviousModuleShouldBeDone() {
		assertTrue( BeforeModuleBootstrapInstaller.moduleIsBusyBeingBootstrapped );
		assertTrue( BeforeModuleBootstrapInstaller.dummyModuleBootstrapped );
		assertFalse( BeforeModuleBootstrapInstaller.moduleIsBootstrapped );
	}

	@Test
	public void afterModuleBootstrappedContextShouldNotBeBootstrapped() {
		assertTrue( AfterModuleBootstrapInstaller.moduleIsBootstrapped );
		assertFalse( AfterModuleBootstrapInstaller.contextIsBootstrapped );
	}

	@Test
	public void afterContextBootstrapped() {
		assertTrue( AfterContextBootstrapInstaller.contextIsBootstrapped );
	}

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public InstallerPhasesModule installerPhasesModule() {
			return new InstallerPhasesModule();
		}

		@Bean
		public AcrossModule dummyModule() {
			return new EmptyAcrossModule( "DummyModule", Object.class );
		}

		@Bean
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.build();
		}
	}

	@AcrossDepends(required = "DummyModule")
	static class InstallerPhasesModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "InstallerPhasesModule";
		}

		@Override
		public String getDescription() {
			return "Has multiple installers for different bootstrap phases.";
		}

		@Override
		public Object[] getInstallers() {
			return new Object[] {
					BeforeContextBootstrapInstaller.class,
					BeforeModuleBootstrapInstaller.class,
					AfterModuleBootstrapInstaller.class,
					AfterContextBootstrapInstaller.class
			};
		}
	}

	@Installer(description = "Run before context bootstrap, no module should be bootstrapped",
			phase = InstallerPhase.BeforeContextBootstrap)
	static class BeforeContextBootstrapInstaller
	{
		static boolean moduleIsBusyBeingBootstrapped = true;
		static boolean dummyModuleBootstrapped = true;

		@Autowired
		private AcrossContextInfo contextInfo;

		@Autowired
		@Module("DummyModule")
		private AcrossModuleInfo dummyModuleInfo;

		@InstallerMethod
		public void install() {
			moduleIsBusyBeingBootstrapped = contextInfo.getModuleBeingBootstrapped() != null;
			dummyModuleBootstrapped = dummyModuleInfo.isBootstrapped();
			installersInOrder.add( getClass() );
		}
	}

	@Installer(description = "Run before module bootstrap, current module should be bootstrapped",
			phase = InstallerPhase.BeforeModuleBootstrap)
	static class BeforeModuleBootstrapInstaller
	{
		static boolean moduleIsBusyBeingBootstrapped = true;
		static boolean moduleIsBootstrapped = true;
		static boolean dummyModuleBootstrapped = false;

		@Autowired
		private AcrossContextInfo contextInfo;

		@Autowired
		@Module("DummyModule")
		private AcrossModuleInfo dummyModuleInfo;

		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModuleInfo currentModuleInfo;

		@InstallerMethod
		public void install() {
			moduleIsBusyBeingBootstrapped
					= StringUtils.equals( "InstallerPhasesModule", contextInfo.getModuleBeingBootstrapped().getName() );
			moduleIsBootstrapped = currentModuleInfo.isBootstrapped();
			dummyModuleBootstrapped = dummyModuleInfo.isBootstrapped();
			installersInOrder.add( getClass() );
		}
	}

	@Installer(description = "Run before module bootstrap, current module should be bootstrapped",
			phase = InstallerPhase.AfterModuleBootstrap)
	static class AfterModuleBootstrapInstaller
	{
		static boolean moduleIsBootstrapped = false;
		static boolean contextIsBootstrapped = true;

		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModuleInfo currentModuleInfo;

		@InstallerMethod
		public void install() {
			moduleIsBootstrapped = currentModuleInfo.isBootstrapped();
			contextIsBootstrapped = currentModuleInfo.getContextInfo().isBootstrapped();
			installersInOrder.add( getClass() );
		}
	}

	@Installer(description = "Run before module bootstrap, current module should be bootstrapped",
			phase = InstallerPhase.AfterContextBootstrap)
	static class AfterContextBootstrapInstaller
	{
		static boolean contextIsBootstrapped = false;

		@Autowired
		private AcrossContextInfo contextInfo;

		@InstallerMethod
		public void install() {
			contextIsBootstrapped = contextInfo.isBootstrapped();
			installersInOrder.add( getClass() );
		}
	}
}
