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
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.InstallerAction;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.modules.exposing.EqualBean;
import test.modules.exposing.ExposingModule;
import test.modules.exposing.MyPrototypeBean;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestAcrossContextBeanRegistry.Config.class })
@DirtiesContext
public class TestAcrossContextBeanRegistry
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void exposedPrototypeBeanShouldBeFoundInRoot() {
		List<MyPrototypeBean> beans = beanRegistry.getBeansOfType( MyPrototypeBean.class );
		assertEquals( 2, beans.size() );
		assertNotNull( beans.get( 0 ) );
	}

	@Test
	public void exposedPrototypeBeanShouldBeFoundWithInternalModulesLookup() {
		List<MyPrototypeBean> beans = beanRegistry.getBeansOfType( MyPrototypeBean.class, true );
		assertEquals( 2, beans.size() );
		assertNotNull( beans.get( 0 ) );
		assertNotNull( beans.get( 1 ) );
	}

	@Test
	public void beansOfTypeShouldReturnAllBeansEvenWhenEqual() {
		assertEquals( 2, beanRegistry.getBeansOfType( EqualBean.class ).size() );
		assertEquals( 2, beanRegistry.getBeansOfType( EqualBean.class, true ).size() );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataSource acrossDataSource() {
			return DataSourceBuilder.create().driverClassName( "org.hsqldb.jdbc.JDBCDriver" ).type( HikariDataSource.class )
			                        .url( "jdbc:hsqldb:mem:acrossTest" ).username( "sa" ).build();
		}

		@Bean
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( new ExposingModule( "default" ) );
			context.addModule( new ExposingModule( "extra" ) );

			context.bootstrap();

			return context;
		}
	}
}
