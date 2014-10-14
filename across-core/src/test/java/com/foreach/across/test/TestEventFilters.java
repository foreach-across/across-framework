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

package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.modules.module1.TestModule1;
import com.foreach.across.test.modules.module2.CustomEventHandlers;
import com.foreach.across.test.modules.module2.NamedEvent;
import com.foreach.across.test.modules.module2.SimpleEvent;
import com.foreach.across.test.modules.module2.TestModule2;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEventFilters.Config.class)
@DirtiesContext
public class TestEventFilters
{
	@Autowired
	private AcrossContext context;

	@Autowired
	private CustomEventHandlers eventHandlers;

	@Test
	public void simpleEventIsNotReceivedByNamedHandlers() {
		SimpleEvent event = new SimpleEvent();

		context.publishEvent( event );

		assertTrue( eventHandlers.getReceivedAll().contains( event ) );
		assertFalse( eventHandlers.getReceivedOne().contains( event ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( event ) );
	}

	@Test
	public void specificNamedEventIsReceivedByMatchingHandlers() {
		NamedEvent event = new NamedEvent( "one" );

		context.publishEvent( event );

		assertTrue( eventHandlers.getReceivedAll().contains( event ) );
		assertTrue( eventHandlers.getReceivedOne().contains( event ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( event ) );

		event = new NamedEvent( "two" );

		context.publishEvent( event );

		assertTrue( eventHandlers.getReceivedAll().contains( event ) );
		assertFalse( eventHandlers.getReceivedOne().contains( event ) );
		assertTrue( eventHandlers.getReceivedTwo().contains( event ) );

		event = new NamedEvent( "three" );

		context.publishEvent( event );

		assertTrue( eventHandlers.getReceivedAll().contains( event ) );
		assertTrue( eventHandlers.getReceivedOne().contains( event ) );
		assertTrue( eventHandlers.getReceivedTwo().contains( event ) );
	}

	@Test
	public void unknownNamedEventIsOnlyReceivedByAllHandler() {
		NamedEvent event = new NamedEvent( "nomatch" );

		context.publishEvent( event );

		assertTrue( eventHandlers.getReceivedAll().contains( event ) );
		assertFalse( eventHandlers.getReceivedOne().contains( event ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( event ) );
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
		@Autowired
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( testModule1() );
			context.addModule( testModule2() );

			return context;
		}

		@Bean
		public TestModule1 testModule1() {
			return new TestModule1();
		}

		@Bean
		public TestModule2 testModule2() {
			return new TestModule2();
		}
	}
}

