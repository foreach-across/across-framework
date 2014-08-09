package com.foreach.across.test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.modules.exposing.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDefaultExposeFilter.Config.class)
@DirtiesContext
public class TestDefaultExposeFilter
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ExposingModule module;

	@Autowired(required=false)
	private MyController myController;

	@Autowired
	private MyService myService;

	@Autowired
	private ExposingConfiguration exposingConfiguration;

	@Autowired(required = false)
	private SimpleConfiguration simpleConfiguration;

	@Test
	public void serviceIsExposedByDefault() {
		assertNotNull( myService );
	}

	@Test
	public void controllerIsNotExposedByDefault() {
		assertNull( myController );
	}

	@Test
	public void configurationCanBeExposed() {
		assertNotNull( exposingConfiguration );
		assertNull( simpleConfiguration );
	}

	@Test
	public void onlyBeansWithExposedAnnotationAreExposed() {
		Map<String, MyBean> exposedBeans = applicationContext.getBeansOfType( MyBean.class );
		Map<String, MyBean> moduleBeans =
				AcrossContextUtils.getApplicationContext( module ).getBeansOfType( MyBean.class );

		assertEquals( 3, exposedBeans.size() );
		assertEquals( 5, moduleBeans.size() );

		assertTrue( exposedBeans.containsKey( "exposedBean" ) );
		assertTrue( exposedBeans.containsKey( "myBeanWithExposed" ) );
		assertTrue( exposedBeans.containsKey( "beanFromExposingConfiguration" ) );
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
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( testModule1() );

			return context;
		}

		@Bean
		public ExposingModule testModule1() {
			return new ExposingModule( "default" );
		}
	}
}
