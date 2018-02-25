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

package test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.filters.ClassBeanFilter;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.database.support.HikariDataSourceHelper;
import test.modules.exposing.*;
import test.modules.module1.SomeInterface;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
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

	@Autowired(required = false)
	private SomeInterface someInterface;

	@Autowired(required = false)
	private LazyExposedBean lazyExposedBean;

	@Test
	public void serviceIsFromServiceModule() {
		assertNotNull( myService );
		assertSame( myService, AcrossContextUtils.getApplicationContext( serviceModule ).getBean( MyService.class ) );
	}

	@Test
	public void controllerIsFromControllerModule() {
		assertNotNull( myController );
		assertSame( myController, AcrossContextUtils.getApplicationContext( controllerModule ).getBean(
				MyController.class ) );
	}

	@Test
	public void noConfigurationIsExposed() {
		assertNull( exposingConfiguration );
		assertNull( simpleConfiguration );
	}

	@Test
	public void beansAreFromMyBeansModule() {
		Map<String, MyBean> exposedBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors( applicationContext,
		                                                                                   MyBean.class );
		Map<String, MyBean> moduleBeans =
				AcrossContextUtils.getApplicationContext( mybeanModule ).getBeansOfType( MyBean.class );

		assertEquals( 5, exposedBeans.size() );
		assertEquals( 5, moduleBeans.size() );
		assertEquals( moduleBeans, exposedBeans );
	}

	@Test
	public void interfaceFromFactoryBeanIsAvailable() {
		assertNotNull( someInterface );
	}

	@Test
	public void lazyExposedBeanIsExposedByBeanName() {
		assertNotNull( lazyExposedBean );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataSource acrossDataSource() throws Exception {
			return HikariDataSourceHelper.create( "org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:mem:acrossTest", "sa",
			                                      StringUtils.EMPTY );
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setInstallerAction( InstallerAction.EXECUTE );

			context.addModule( serviceModule() );
			context.addModule( controllerModule() );
			context.addModule( mybeanModule() );

			context.bootstrap();

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
			module.expose( Controller.class );
			module.expose( "lazyExposedBean" );

			return module;
		}

		@Bean
		public ExposingModule mybeanModule() {
			ExposingModule module = new ExposingModule( "mybean" );
			module.setExposeFilter( new ClassBeanFilter( MyBean.class ) );
			module.expose( SomeInterface.class );

			return module;
		}
	}
}
