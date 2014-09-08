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
				                 "sessionFactory",
				                 Object.class
		                 )
		);
		definitions.put( "transactionManager",
		                 new ExposedBeanDefinition(
				                 mock( AcrossContextBeanRegistry.class ),
				                 "module",
				                 "transactionManager",
				                 Object.class
		                 )
		);
	}

	@Test
	public void testCamelCasing() {
		ExposedBeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "test" );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "testTransactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
	}

	@Test
	public void testNoCamelCasing() {
		ExposedBeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "some.", false );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "some.sessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "some.transactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
	}
}
