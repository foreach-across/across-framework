package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.transformers.BeanDefinitionTransformerComposite;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import com.foreach.across.core.transformers.BeanRenameTransformer;
import com.foreach.across.core.transformers.PrimaryBeanTransformer;
import com.foreach.across.test.modules.exposing.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestExposeTransformer.Config.class)
@DirtiesContext
public class TestExposeTransformer
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ExposingModule primaryModule;

	@Autowired
	private ExposingModule prefixedModule;

	@Autowired
	private MyController someController;

	@Autowired
	@Qualifier("someService")
	private MyService someService;

	@Autowired(required = false)
	private ExposingConfiguration exposingConfiguration;

	@Autowired(required = false)
	private SimpleConfiguration simpleConfiguration;

	@Test
	public void serviceIsFromPrimaryModule() {
		assertNotNull( someService );
		assertSame( someService, AcrossContextUtils.getBeanOfType( primaryModule, MyService.class ) );
	}

	@Test
	public void controllerIsfromPrimaryModule() {
		assertNotNull( someController );
		assertSame( someController, AcrossContextUtils.getBeanOfType( primaryModule, MyController.class ) );
	}

	@Test
	public void configurationIsFromPrimaryModule() {
		assertNotNull( exposingConfiguration );
		assertSame( exposingConfiguration,
		            AcrossContextUtils.getBeanOfType( primaryModule, ExposingConfiguration.class ) );
		assertNull( simpleConfiguration );
	}

	@Test
	public void beansAreFromMyBeansModule() {
		Map<String, MyBean> exposedBeans = applicationContext.getBeansOfType( MyBean.class );

		assertEquals( 6, exposedBeans.size() );
		assertTrue( exposedBeans.containsKey( "exposedBean" ) );
		assertTrue( exposedBeans.containsKey( "myBeanWithExposed" ) );
		assertTrue( exposedBeans.containsKey( "beanFromExposingConfiguration" ) );
		assertTrue( exposedBeans.containsKey( "prefixExposedBean" ) );
		assertTrue( exposedBeans.containsKey( "prefixMyBeanWithExposed" ) );
		assertTrue( exposedBeans.containsKey( "prefixBeanFromExposingConfiguration" ) );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataSource acrossDataSource() throws Exception {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:acrossTest" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setAllowInstallers( false );

			context.addModule( primaryModule() );
			context.addModule( prefixedModule() );

			return context;
		}

		@Bean
		public ExposingModule primaryModule() {
			ExposingModule module = new ExposingModule( "primary" );

			Map<String, String> rename = new HashMap<String, String>();
			rename.put( "myService", "someService" );

			Collection<String> primaries = Arrays.asList( "myController" );
			module.setExposeTransformer(
					new BeanDefinitionTransformerComposite( new PrimaryBeanTransformer( primaries ),
					                                        new BeanRenameTransformer( rename, false ) ) );

			return module;
		}

		@Bean
		public ExposingModule prefixedModule() {
			ExposingModule module = new ExposingModule( "prefixed" );

			Collection<String> primaries = Arrays.asList( "myService" );
			module.setExposeTransformer(
					new BeanDefinitionTransformerComposite( new PrimaryBeanTransformer( primaries ),
					                                        new BeanPrefixingTransformer( "prefix" ) ) );

			return module;
		}
	}
}


