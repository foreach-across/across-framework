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

package test.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.BeanRenameTransformer;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestBeanRenameTransformer
{
	private final Map<String, ExposedBeanDefinition> definitions = new HashMap<>();

	public TestBeanRenameTransformer() {
		definitions.put( "sessionFactory",
		                 new ExposedBeanDefinition( mock( AcrossContextBeanRegistry.class ), "module", 0,
		                                            "sessionFactory", Object.class, new String[0] ) );
		definitions.put( "transactionManager",
		                 new ExposedBeanDefinition( mock( AcrossContextBeanRegistry.class ), "module", 0,
		                                            "transactionManager",
		                                            Object.class, new String[] { "currentTransactionManager" } ) );
	}

	@Test
	public void renameWithoutRemovals() {
		Map<String, String> renames = new HashMap<>();
		renames.put( "sessionFactory", "testSessionFactory" );

		ExposedBeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, false );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "transactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
	}

	@Test
	public void renameAlias() {
		Map<String, String> renames = new HashMap<>();
		renames.put( "currentTransactionManager", "myTransactionManager" );

		ExposedBeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, false );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "sessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "transactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
		assertEquals(
				Collections.singleton( "myTransactionManager" ),
				definitions.get( "transactionManager" ).getAliases()
		);
	}

	@Test
	public void renameWithRemovals() {
		Map<String, String> renames = new HashMap<>();
		renames.put( "sessionFactory", "testSessionFactory" );

		ExposedBeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, true );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 1, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
	}
}
