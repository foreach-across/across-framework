package com.foreach.across.test.transformers;

import com.foreach.across.core.transformers.BeanDefinitionTransformer;
import com.foreach.across.core.transformers.BeanRenameTransformer;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBeanRenameTransformer
{
	private final Map<String, Object> singletons = new HashMap<String, Object>();
	private final Map<String, BeanDefinition> definitions = new HashMap<String, BeanDefinition>();

	public TestBeanRenameTransformer() {
		singletons.put( "sessionFactory", "" );
		singletons.put( "org.springframework.somebean", "" );

		definitions.put( "sessionFactory", new GenericBeanDefinition() );
		definitions.put( "transactionManager", new GenericBeanDefinition() );
	}

	@Test
	public void renameWithoutRemovals() {
		Map<String, String> renames = new HashMap<String, String>();
		renames.put( "sessionFactory", "testSessionFactory" );

		BeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, false );

		Map<String, Object> modifiedSingletons = transformer.transformSingletons( singletons );
		assertEquals( 2, modifiedSingletons.size() );
		assertTrue( modifiedSingletons.containsKey( "testSessionFactory" ) );
		assertTrue( modifiedSingletons.containsKey( "org.springframework.somebean" ) );

		Map<String, BeanDefinition> modifiedDefinitions = transformer.transformBeanDefinitions( definitions );
		assertEquals( 2, modifiedDefinitions.size() );
		assertTrue( modifiedDefinitions.containsKey( "testSessionFactory" ) );
		assertTrue( modifiedDefinitions.containsKey( "transactionManager" ) );
	}

	@Test
	public void renameWithRemovals() {
		Map<String, String> renames = new HashMap<String, String>();
		renames.put( "sessionFactory", "testSessionFactory" );

		BeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, true );

		Map<String, Object> modifiedSingletons = transformer.transformSingletons( singletons );
		assertEquals( 1, modifiedSingletons.size() );
		assertTrue( modifiedSingletons.containsKey( "testSessionFactory" ) );

		Map<String, BeanDefinition> modifiedDefinitions = transformer.transformBeanDefinitions( definitions );
		assertEquals( 1, modifiedDefinitions.size() );
		assertTrue( modifiedDefinitions.containsKey( "testSessionFactory" ) );
	}
}
