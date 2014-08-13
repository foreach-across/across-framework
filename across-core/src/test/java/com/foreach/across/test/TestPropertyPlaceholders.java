package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.PropertyPlaceholderSupportConfigurer;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.modules.properties.PropertiesModule;
import com.foreach.across.test.modules.properties.SetPropertyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test property setting and spring expression language conditionals.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPropertyPlaceholders.Config.class)
@DirtiesContext
public class TestPropertyPlaceholders
{
	@Autowired
	private AcrossContextBeanRegistry contextBeanRegistry;

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
		assertEquals( "acrossContext", config.getProperty( "contextValue" ) );
		assertEquals( "acrossContext", config.getProperty( "moduleSourceValue" ) );
		assertEquals( "acrossContext", config.getProperty( "moduleDirectValue" ) );
		assertEquals( new Integer( 777 ), config.getProperty( "contextDirectValue", Integer.class ) );

		config = contextBeanRegistry.getBeanOfTypeFromModule( "sourceOnModule", SetPropertyConfig.class );

		assertNotNull( config );
		assertEquals( "acrossContext", config.contextValue );
		assertEquals( "acrossModule", config.moduleSourceValue );
		assertEquals( "acrossModule", config.moduleDirectValue );
		assertEquals( 777, config.contextDirectValue );
		assertEquals( 50, config.unresolvable );
		assertEquals( "acrossContext", config.getProperty( "contextValue" ) );
		assertEquals( "acrossModule", config.getProperty( "moduleSourceValue" ) );
		assertEquals( "acrossModule", config.getProperty( "moduleDirectValue" ) );
		assertEquals( new Integer( 777 ), config.getProperty( "contextDirectValue", Integer.class ) );

		config = contextBeanRegistry.getBeanOfTypeFromModule( "directOnModule", SetPropertyConfig.class );

		assertNotNull( config );

		assertEquals( "acrossContext", config.contextValue );
		assertEquals( "acrossModule", config.moduleSourceValue );
		assertEquals( "directValue", config.moduleDirectValue );
		assertEquals( 777, config.contextDirectValue );
		assertEquals( 100, config.unresolvable );
		assertEquals( "acrossContext", config.getProperty( "contextValue" ) );
		assertEquals( "acrossModule", config.getProperty( "moduleSourceValue" ) );
		assertEquals( "directValue", config.getProperty( "moduleDirectValue" ) );
		assertEquals( new Integer( 777 ), config.getProperty( "contextDirectValue", Integer.class ) );
	}

	@Configuration
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
					new ClassPathResource( "com/foreach/across/test/TestPropertiesContext.properties" ) );

			acrossContext.setProperty( "contextDirectValue", 777 );

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
					new ClassPathResource( "com/foreach/across/test/TestPropertiesModule.properties" ) );
			return module;
		}

		@Bean
		public PropertiesModule directOnModule() {
			PropertiesModule module = new PropertiesModule( "directOnModule" );
			module.addPropertySources(
					new ClassPathResource( "com/foreach/across/test/TestPropertiesModule.properties" ),
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
