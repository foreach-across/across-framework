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
