package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.config.PropertyPlaceholderSupportConfiguration;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
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
@ContextConfiguration(classes = TestPropertiesAndElConditional.Config.class)
@DirtiesContext
public class TestPropertiesAndElConditional
{
	@Autowired
	private PropertiesModule onlyFromContext;

	@Autowired
	private PropertiesModule sourceOnModule;

	@Autowired
	private PropertiesModule directOnModule;

	@Test
	public void checkPropertiesSet() {
		SetPropertyConfig config = AcrossContextUtils.getBeanOfType( onlyFromContext, SetPropertyConfig.class );

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

		config = AcrossContextUtils.getBeanOfType( sourceOnModule, SetPropertyConfig.class );

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

		config = AcrossContextUtils.getBeanOfType( directOnModule, SetPropertyConfig.class );

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
			acrossContext.setAllowInstallers( false );

			acrossContext.addModule( directOnModule() );
			acrossContext.addModule( sourceOnModule() );
			acrossContext.addModule( onlyFromContext() );

			acrossContext.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer( PropertyPlaceholderSupportConfiguration.class ),
					ConfigurerScope.CONTEXT_AND_MODULES );

			acrossContext.addPropertySources(
					new ClassPathResource( "com/foreach/across/test/TestPropertiesContext.properties" ) );

			acrossContext.setProperty( "contextDirectValue", 777 );

			return acrossContext;
		}

		@Bean
		public PropertiesModule onlyFromContext() {
			return new PropertiesModule( "moduleOne" );
		}

		@Bean
		public PropertiesModule sourceOnModule() throws Exception {
			PropertiesModule module = new PropertiesModule( "moduleTwo" );
			module.addPropertySources(
					new ClassPathResource( "com/foreach/across/test/TestPropertiesModule.properties" ) );
			return module;
		}

		@Bean
		public PropertiesModule directOnModule() throws Exception {
			PropertiesModule module = new PropertiesModule( "moduleThree" );
			module.addPropertySources(
					new ClassPathResource( "com/foreach/across/test/TestPropertiesModule.properties" ),
					new ClassPathResource( "com/foreach/across/test/NotExistingProperties.properties" ) );
			module.setProperty( "moduleDirectValue", "directValue" );
			module.setProperty( "unresolvable", 100 );
			return module;
		}
	}
}
