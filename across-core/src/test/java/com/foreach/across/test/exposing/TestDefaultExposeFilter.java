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

package com.foreach.across.test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.database.support.HikariDataSourceHelper;
import com.foreach.across.test.modules.exposing.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
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
import java.util.concurrent.atomic.AtomicReference;

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

	@Autowired(required = false)
	private MyController myController;

	@Autowired
	private MyService myService;

	@Autowired
	private ExposingConfiguration exposingConfiguration;

	@Autowired(required = false)
	private SimpleConfiguration simpleConfiguration;

	@Autowired(required = false)
	private AtomicReference<Integer> integerReference;

	@Autowired(required = false)
	private AtomicReference<String> stringReference;

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
		Map<String, MyBean> exposedBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors( applicationContext,
		                                                                                   MyBean.class );
		Map<String, MyBean> moduleBeans =
				AcrossContextUtils.getApplicationContext( module ).getBeansOfType( MyBean.class );

		assertEquals( 3, exposedBeans.size() );
		assertEquals( 5, moduleBeans.size() );

		assertTrue( exposedBeans.containsKey( "exposedBean" ) );
		assertTrue( exposedBeans.containsKey( "myBeanWithExposed" ) );
		assertTrue( exposedBeans.containsKey( "beanFromExposingConfiguration" ) );
	}

	@Test
	public void aliasShouldBeExposedAsWell() {
		MyBean bean = applicationContext.getBean( "exposedBean", MyBean.class );
		MyBean aliased = applicationContext.getBean( "aliasedExposedBean", MyBean.class );

		assertSame( bean, aliased );
	}

	@Test
	public void autowiredGenericObject() {
		assertNotNull( integerReference );
		assertNotNull( stringReference );
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
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( testModule1() );

			context.bootstrap();

			return context;
		}

		@Bean
		public ExposingModule testModule1() {
			return new ExposingModule( "default" );
		}
	}
}
