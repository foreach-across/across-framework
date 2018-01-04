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

package com.foreach.across.test.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestBeanPrefixingTransformer
{
	private final Map<String, ExposedBeanDefinition> definitions = new HashMap<>();

	public TestBeanPrefixingTransformer() {
		definitions.put( "sessionFactory", new ExposedBeanDefinition(
				mock( AcrossContextBeanRegistry.class ),
				"module",
				0,
				"sessionFactory",
				Object.class,
				new String[0] )
		);
		definitions.put( "transactionManager",
		                 new ExposedBeanDefinition(
				                 mock( AcrossContextBeanRegistry.class ),
				                 "module",
				                 0,
				                 "transactionManager",
				                 Object.class,
				                 new String[] { "myTransactionManager" } )
		);
	}

	@Test
	public void camelCasing() {
		ExposedBeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "test" );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "testTransactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
		assertEquals(
				Collections.singleton( "testMyTransactionManager" ),
				definitions.get( "transactionManager" ).getAliases()
		);
	}

	@Test
	public void noCamelCasing() {
		ExposedBeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "some.", false );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "some.sessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "some.transactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
		assertEquals(
				Collections.singleton( "some.myTransactionManager" ),
				definitions.get( "transactionManager" ).getAliases()
		);
	}
}
