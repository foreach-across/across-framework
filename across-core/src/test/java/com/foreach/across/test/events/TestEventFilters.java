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

package com.foreach.across.test.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.database.support.HikariDataSourceHelper;
import com.foreach.across.test.modules.module1.TestModule1;
import com.foreach.across.test.modules.module2.*;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

	@SuppressWarnings("unchecked")
	@Test
	public void specificTypedEventsAreReceivedByAllMatchingHandlers() {
		GenericEvent<Long, HashMap> longMap = new GenericEvent<>( Long.class, HashMap.class );
		GenericEvent<Integer, List<Integer>> integerList = new GenericEvent<>(
				Integer.class,
				ResolvableType.forClassWithGenerics( ArrayList.class, Integer.class )
		);
		GenericEvent<Integer, List<Long>> longList = new GenericEvent<>(
				Integer.class,
				ResolvableType.forClassWithGenerics( ArrayList.class, Long.class )
		);
		GenericEvent<BigDecimal, Set> decimalSet = new GenericEvent<>( BigDecimal.class, Set.class );

		context.publishEvent( longMap );
		context.publishEvent( integerList );
		context.publishEvent( longList );
		context.publishEvent( decimalSet );

		assertTrue( eventHandlers.getReceivedAll().contains( longMap ) );
		assertFalse( eventHandlers.getReceivedOne().contains( longMap ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( longMap ) );
		assertTrue( eventHandlers.getReceivedTypedLongMap().contains( longMap ) );
		assertFalse( eventHandlers.getReceivedTypedIntegerList().contains( longMap ) );
		assertFalse( eventHandlers.getReceivedTypedNumberCollection().contains( longMap ) );

		assertTrue( eventHandlers.getReceivedAll().contains( integerList ) );
		assertFalse( eventHandlers.getReceivedOne().contains( integerList ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( integerList ) );
		assertFalse( eventHandlers.getReceivedTypedLongMap().contains( integerList ) );
		assertTrue( eventHandlers.getReceivedTypedIntegerList().contains( integerList ) );
		assertTrue( eventHandlers.getReceivedTypedNumberCollection().contains( integerList ) );

		assertTrue( eventHandlers.getReceivedAll().contains( longList ) );
		assertFalse( eventHandlers.getReceivedOne().contains( longList ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( longList ) );
		assertFalse( eventHandlers.getReceivedTypedLongMap().contains( longList ) );
		assertFalse( eventHandlers.getReceivedTypedIntegerList().contains( longList ) );
		assertTrue( eventHandlers.getReceivedTypedNumberCollection().contains( longList ) );

		assertTrue( eventHandlers.getReceivedAll().contains( decimalSet ) );
		assertFalse( eventHandlers.getReceivedOne().contains( decimalSet ) );
		assertFalse( eventHandlers.getReceivedTwo().contains( decimalSet ) );
		assertFalse( eventHandlers.getReceivedTypedLongMap().contains( decimalSet ) );
		assertFalse( eventHandlers.getReceivedTypedIntegerList().contains( decimalSet ) );
		assertTrue( eventHandlers.getReceivedTypedNumberCollection().contains( decimalSet ) );
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
		@Autowired
		public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setDataSource( acrossDataSource() );
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( testModule1() );
			context.addModule( testModule2() );

			context.bootstrap();

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

