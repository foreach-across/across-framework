package com.foreach.across.test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.filters.ClassBeanFilter;
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
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestCustomExposeFilter.Config.class)
@DirtiesContext
public class TestCustomExposeFilter
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ExposingModule serviceModule;

	@Autowired
	private ExposingModule controllerModule;

	@Autowired
	private ExposingModule mybeanModule;

	@Autowired
	private MyController myController;

	@Autowired
	private MyService myService;

	@Autowired(required = false)
	private ExposingConfiguration exposingConfiguration;

	@Autowired(required = false)
	private SimpleConfiguration simpleConfiguration;

	@Test
	public void serviceIsFromServiceModule() {
		assertNotNull( myService );
		assertSame( myService, AcrossContextUtils.getBeanOfType( serviceModule, MyService.class ) );
	}

	@Test
	public void controllerIsFromControllerModule() {
		assertNotNull( myController );
		assertSame( myController, AcrossContextUtils.getBeanOfType( controllerModule, MyController.class ) );
	}

	@Test
	public void noConfigurationIsExposed() {
		assertNull( exposingConfiguration );
		assertNull( simpleConfiguration );
	}

	@Test
	public void beansAreFromMyBeansModule() {
		Map<String, MyBean> exposedBeans = applicationContext.getBeansOfType( MyBean.class );
		Map<String, MyBean> moduleBeans =
				AcrossContextUtils.getApplicationContext( mybeanModule ).getBeansOfType( MyBean.class );

		assertEquals( 5, exposedBeans.size() );
		assertEquals( 5, moduleBeans.size() );
		assertEquals( moduleBeans, exposedBeans );
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
			context.setInstallerAction( InstallerAction.EXECUTE );

			context.addModule( serviceModule() );
			context.addModule( controllerModule() );
			context.addModule( mybeanModule() );

			return context;
		}

		@Bean
		public ExposingModule serviceModule() {
			ExposingModule module = new ExposingModule( "service" );
			module.setExposeFilter( new AnnotationBeanFilter( Service.class ) );

			return module;
		}

		@Bean
		public ExposingModule controllerModule() {
			ExposingModule module = new ExposingModule( "controller" );
			module.setExposeFilter( new AnnotationBeanFilter( Controller.class ) );

			return module;
		}

		@Bean
		public ExposingModule mybeanModule() {
			ExposingModule module = new ExposingModule( "mybean" );
			module.setExposeFilter( new ClassBeanFilter( MyBean.class ) );

			return module;
		}
	}
}
