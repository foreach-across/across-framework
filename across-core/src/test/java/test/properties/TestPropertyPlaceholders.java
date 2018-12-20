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

package test.properties;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.PropertyPlaceholderSupportConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.InstallerAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.properties.settings.PropertiesModule;
import test.properties.settings.SetPropertyConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DevelopmentModeCondition property setting and spring expression language conditionals.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPropertyPlaceholders.Config.class)
@DirtiesContext
public class TestPropertyPlaceholders
{
	@Autowired
	private AcrossContextBeanRegistry contextBeanRegistry;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void checkPropertiesSet() {
		SetPropertyConfig config = contextBeanRegistry.getBeanOfTypeFromModule( "onlyFromContext",
		                                                                        SetPropertyConfig.class );

		assertNotNull( config );
		assertEquals( "acrossContext", config.contextValue );
		assertEquals( "acrossContext", config.moduleSourceValue );
		assertEquals( "acrossContext", config.moduleDirectValue );
		assertEquals( 777, config.contextDirectValue );
		assertEquals( 50, config.unresolvable );
		assertEquals( "acrossContext", config.getSettings().getContextValue() );
		assertEquals( "acrossContext", config.getSettings().getModuleSourceValue() );
		assertEquals( "acrossContext", config.getSettings().getModuleDirectValue() );
		// todo: assertEquals( new Integer( 777 ), config.getSettings().getContextValue() );

		config = contextBeanRegistry.getBeanOfTypeFromModule( "sourceOnModule", SetPropertyConfig.class );

		assertNotNull( config );
		assertEquals( "acrossContext", config.contextValue );
		assertEquals( "acrossModule", config.moduleSourceValue );
		assertEquals( "acrossModule", config.moduleDirectValue );
		assertEquals( 777, config.contextDirectValue );
		assertEquals( 50, config.unresolvable );
		assertEquals( "acrossContext", config.getSettings().getContextValue() );
		assertEquals( "acrossModule", config.getSettings().getModuleSourceValue() );
		assertEquals( "acrossModule", config.getSettings().getModuleDirectValue() );
		//assertEquals( new Integer( 777 ), config.getProperty( "contextDirectValue", Integer.class ) );

		config = contextBeanRegistry.getBeanOfTypeFromModule( "directOnModule", SetPropertyConfig.class );

		assertNotNull( config );

		assertEquals( "acrossContext", config.contextValue );
		assertEquals( "acrossModule", config.moduleSourceValue );
		assertEquals( "directValue", config.moduleDirectValue );
		assertEquals( 777, config.contextDirectValue );
		assertEquals( 100, config.unresolvable );
		assertEquals( "acrossContext", config.getSettings().getContextValue() );
		assertEquals( "acrossModule", config.getSettings().getModuleSourceValue() );
		assertEquals( "directValue", config.getSettings().getModuleDirectValue() );
		//assertEquals( new Integer( 777 ), config.getProperty( "contextDirectValue", Integer.class ) );

		assertEquals( "default", config.getSettings().getDefaultOnlyValue() );
		// todo: assertEquals( "applicationContext", config.getProperty( "parentContextValue" ) );
	}

	@Test
	public void propertySourceOrder() {
		ConfigurableEnvironment env = (ConfigurableEnvironment) contextInfo.getModuleInfo( "directOnModule" )
		                                                                   .getApplicationContext().getEnvironment();

		assertNotNull( env );

		MutablePropertySources sources = env.getPropertySources();
		assertNotNull( sources );
		assertEquals( 8, sources.size() );

		assertEquals( 5, sources.precedenceOf( sources.get( StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME ) ) );
		assertEquals( 6, sources.precedenceOf( sources.get( StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME ) ) );
	}

	@Configuration
	@org.springframework.context.annotation.PropertySource(
			value = "test/TestParentProperties.properties")
	public static class Config
	{
		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext acrossContext = new AcrossContext( applicationContext );
			acrossContext.setInstallerAction( InstallerAction.DISABLED );

			acrossContext.addModule( directOnModule() );
			acrossContext.addModule( sourceOnModule() );
			acrossContext.addModule( onlyFromContext() );
			acrossContext.addModule( moduleFour() );

			acrossContext.addApplicationContextConfigurer( new PropertyPlaceholderSupportConfigurer(),
			                                               ConfigurerScope.CONTEXT_AND_MODULES );

			acrossContext.addPropertySources(
					new ClassPathResource( "test/TestPropertiesContext.properties" ) );

			acrossContext.setProperty( "contextDirectValue", 777 );

			acrossContext.bootstrap();

			return acrossContext;
		}

		@Bean
		public PropertiesModule onlyFromContext() {
			return new PropertiesModule( "onlyFromContext" );
		}

		@Bean
		public PropertiesModule sourceOnModule() {
			PropertiesModule module = new PropertiesModule( "sourceOnModule" );
			module.addPropertySources(
					new ClassPathResource( "test/TestPropertiesModule.properties" ) );
			return module;
		}

		@Bean
		public PropertiesModule directOnModule() {
			PropertiesModule module = new PropertiesModule( "directOnModule" );
			module.addPropertySources(
					new ClassPathResource( "test/TestPropertiesModule.properties" ),
					new ClassPathResource( "com/foreach/across/test/NotExistingProperties.properties" ) );
			module.setProperty( "moduleDirectValue", "directValue" );
			module.setProperty( "unresolvable", 100 );
			return module;
		}

		@Bean
		public PropertiesModule moduleFour() {
			return new PropertiesModule( "moduleFour" );
		}
	}
}
