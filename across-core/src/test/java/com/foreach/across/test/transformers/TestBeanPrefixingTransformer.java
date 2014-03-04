package com.foreach.across.test.transformers;

import com.foreach.across.core.transformers.BeanDefinitionTransformer;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBeanPrefixingTransformer
{
	private final Map<String, Object> singletons = new HashMap<String, Object>();
	private final Map<String, BeanDefinition> definitions = new HashMap<String, BeanDefinition>();

	public TestBeanPrefixingTransformer() {
		singletons.put( "sessionFactory", "" );
		singletons.put( "org.springframework.somebean", "" );

		definitions.put( "sessionFactory", new GenericBeanDefinition() );
		definitions.put( "transactionManager", new GenericBeanDefinition() );
	}

	@Test
	public void testCamelCasing() {
		BeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "test" );

		Map<String, Object> modifiedSingletons = transformer.transformSingletons( singletons );
		assertEquals( 2, modifiedSingletons.size() );
		assertTrue( modifiedSingletons.containsKey( "testSessionFactory" ) );
		assertTrue( modifiedSingletons.containsKey( "testOrg.springframework.somebean" ) );

		Map<String, BeanDefinition> modifiedDefinitions = transformer.transformBeanDefinitions( definitions );
		assertEquals( 2, modifiedDefinitions.size() );
		assertTrue( modifiedDefinitions.containsKey( "testSessionFactory" ) );
		assertTrue( modifiedDefinitions.containsKey( "testTransactionManager" ) );
	}

	@Test
	public void testNoCamelCasing() {
		BeanDefinitionTransformer transformer = new BeanPrefixingTransformer( "some.", false );

		Map<String, Object> modifiedSingletons = transformer.transformSingletons( singletons );
		assertEquals( 2, modifiedSingletons.size() );
		assertTrue( modifiedSingletons.containsKey( "some.sessionFactory" ) );
		assertTrue( modifiedSingletons.containsKey( "some.org.springframework.somebean" ) );

		Map<String, BeanDefinition> modifiedDefinitions = transformer.transformBeanDefinitions( definitions );
		assertEquals( 2, modifiedDefinitions.size() );
		assertTrue( modifiedDefinitions.containsKey( "some.sessionFactory" ) );
		assertTrue( modifiedDefinitions.containsKey( "some.transactionManager" ) );
	}
}
