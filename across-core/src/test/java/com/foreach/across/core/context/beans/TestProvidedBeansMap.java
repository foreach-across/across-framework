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

package com.foreach.across.core.context.beans;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestProvidedBeansMap
{
	private ProvidedBeansMap beans;

	@Before
	public void reset() {
		beans = new ProvidedBeansMap();
	}

	@Test
	public void singletons() {
		String one = "one";
		Integer two = 2;

		beans.put( "one", one );
		beans.put( "two", two );

		assertTrue( beans.getBeanDefinitions().isEmpty() );

		Map<String, Object> singletons = beans.getSingletons();
		assertEquals( 2, singletons.size() );
		assertSame( one, singletons.get( "one" ) );
		assertSame( two, singletons.get( "two" ) );
	}

	@Test
	public void definitions() {
		BeanDefinition one = mock( BeanDefinition.class );
		BeanDefinition two = new GenericBeanDefinition();

		beans.put( "one", one );
		beans.put( "two", two );

		assertTrue( beans.getSingletons().isEmpty() );

		Map<String, BeanDefinition> definitions = beans.getBeanDefinitions();
		assertEquals( 2, definitions.size() );
		assertSame( one, definitions.get( "one" ) );
		assertSame( two, definitions.get( "two" ) );
	}

	@Test
	public void definitionAsSingleton() {
		BeanDefinition two = new GenericBeanDefinition();

		beans.put( "two", new SingletonBean( two ) );

		assertTrue( beans.getBeanDefinitions().isEmpty() );

		Map<String, Object> singletons = beans.getSingletons();
		assertEquals( 1, singletons.size() );
		assertSame( two, singletons.get( "two" ) );
	}

	@Test
	public void singletonWithDefinition() {
		String one = "one";
		BeanDefinition two = new GenericBeanDefinition();

		beans.put( "two", new SingletonBean( one, two ) );

		Map<String, BeanDefinition> definitions = beans.getBeanDefinitions();
		assertEquals( 1, definitions.size() );
		assertSame( two, definitions.get( "two" ) );

		Map<String, Object> singletons = beans.getSingletons();
		assertEquals( 1, singletons.size() );
		assertSame( one, singletons.get( "two" ) );
	}

	@Test
	public void primarySingletons() {
		String one = "one";

		beans.put( "two", new PrimarySingletonBean( one ) );
		Map<String, Object> singletons = beans.getSingletons();
		assertEquals( 1, singletons.size() );
		assertSame( one, singletons.get( "two" ) );

		Map<String, BeanDefinition> definitions = beans.getBeanDefinitions();
		assertEquals( 1, definitions.size() );

		BeanDefinition definition = definitions.get( "two" );
		assertNotNull( definition );
		assertTrue( definition.isSingleton() );
		assertTrue( definition.isPrimary() );
	}

	@Test
	public void mixedSingletonsAndDefinitions() {
		String one = "one";
		Integer two = 2;
		BigDecimal three = new BigDecimal( "0" );
		Object four = new Object();
		BeanDefinition defFive = mock( BeanDefinition.class );
		BeanDefinition defFour = new GenericBeanDefinition();

		beans.put( "one", one );
		beans.put( "two", new SingletonBean( two ) );
		beans.put( "three", new PrimarySingletonBean( three ) );
		beans.put( "four", new SingletonBean( four, defFour ) );
		beans.put( "five", defFive );

		Map<String, Object> singletons = beans.getSingletons();
		assertEquals( 4, singletons.size() );
		assertSame( one, singletons.get( "one" ) );
		assertSame( two, singletons.get( "two" ) );
		assertSame( three, singletons.get( "three" ) );
		assertSame( four, singletons.get( "four" ) );

		Map<String, BeanDefinition> definitions = beans.getBeanDefinitions();
		assertEquals( 3, definitions.size() );
		assertSame( defFour, definitions.get( "four" ) );
		assertSame( defFive, definitions.get( "five" ) );
		assertNotNull( definitions.get( "three" ) );
	}
}
