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
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.installers.InstallerPhase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestInstallerBeansAndProperties.Config.class)
@TestPropertySource(properties = { "parent.value=parent", "module.value=parent" })
public class TestInstallerBeansAndProperties
{
	private static List<Class> installersInOrder = new ArrayList<>();

	@Test
	public void runInstallerTests() {
		// Actual assertions are done inside the installers
		assertEquals(
				Arrays.asList( BeforeContextBootstrapInstaller.class, BeforeModuleBootstrapInstaller.class,
				               AfterModuleBootstrapInstaller.class ),
				installersInOrder
		);
	}

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public InstallerBeansTestModule installerBeansTestModule() {
			InstallerBeansTestModule testModule = new InstallerBeansTestModule();
			testModule.setProperty( "module.value", "module" );
			return testModule;
		}

		@Bean
		public EmbeddedDatabase acrossDataSource() {
			return new EmbeddedDatabaseBuilder()
					.setType( EmbeddedDatabaseType.HSQL )
					.setName( "core" )
					.build();
		}

		@Bean
		public String moduleBean() {
			return "parentModuleBean";
		}
	}

	static class InstallerBeansTestModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "InstallerBeansTestModule";
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
					AfterModuleBootstrapInstaller.class
			};
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> applicationContextConfigurers ) {
			applicationContextConfigurers.add( new AnnotatedClassConfigurer( ModuleBeans.class ) );
		}

		@Override
		protected void registerDefaultInstallerContextConfigurers( Set<ApplicationContextConfigurer> installerContextConfigurers ) {
			installerContextConfigurers.add( new AnnotatedClassConfigurer( InstallerBeans.class ) );
		}
	}

	@Configuration
	@Lazy
	static class InstallerBeans
	{
		@Bean
		public String myInstallerBean() {
			return "myInstallerBean";
		}
	}

	@Configuration
	static class ModuleBeans
	{
		@Bean
		public String moduleBean() {
			return "moduleBean";
		}

		@Bean
		public String myInstallerBean() {
			return "moduleInstallerBean";
		}
	}

	@Installer(description = "", phase = InstallerPhase.BeforeContextBootstrap)
	static class BeforeContextBootstrapInstaller
	{
		private final String myInstallerBean, moduleBean;

		@Value("${parent.value}")
		private String parentValue;

		@Value("${module.value}")
		private String moduleValue;

		@Autowired
		public BeforeContextBootstrapInstaller( String myInstallerBean, String moduleBean ) {
			this.myInstallerBean = myInstallerBean;
			this.moduleBean = moduleBean;
		}

		@InstallerMethod
		public void install() {
			assertEquals( "myInstallerBean", myInstallerBean );
			assertEquals( "parentModuleBean", moduleBean );
			assertEquals( "parent", parentValue );
			assertEquals( "module", moduleValue );
			installersInOrder.add( getClass() );
		}
	}

	@Installer(description = "", phase = InstallerPhase.BeforeModuleBootstrap)
	static class BeforeModuleBootstrapInstaller
	{
		@Autowired(required = false)
		private BeforeContextBootstrapInstaller beforeInstaller;

		@Autowired
		private String myInstallerBean;

		@Autowired
		private String moduleBean;

		@Value("${parent.value}")
		private String parentValue;

		@Value("${module.value}")
		private String moduleValue;

		@InstallerMethod
		public void install() {
			assertNotNull( beforeInstaller );
			assertEquals( "myInstallerBean", myInstallerBean );
			assertEquals( "parentModuleBean", moduleBean );
			assertEquals( "parent", parentValue );
			assertEquals( "module", moduleValue );
			installersInOrder.add( getClass() );
		}
	}

	@Installer(description = "", phase = InstallerPhase.AfterModuleBootstrap)
	static class AfterModuleBootstrapInstaller
	{
		@Autowired
		private String myInstallerBean;

		@Autowired
		private String moduleBean;

		@Value("${parent.value}")
		private String parentValue;

		@Value("${module.value}")
		private String moduleValue;

		@InstallerMethod
		public void install() {
			assertEquals( "myInstallerBean", myInstallerBean );
			assertEquals( "moduleBean", moduleBean );
			assertEquals( "parent", parentValue );
			assertEquals( "module", moduleValue );
			installersInOrder.add( getClass() );
		}
	}
}
