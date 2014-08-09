package com.foreach.across.test.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.transformers.BeanRenameTransformer;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestBeanRenameTransformer
{
	private final Map<String, ExposedBeanDefinition> definitions = new HashMap<>();

	public TestBeanRenameTransformer() {
		definitions.put( "sessionFactory",
		                 new ExposedBeanDefinition( mock( AcrossContextBeanRegistry.class ), "module", "sessionFactory",
		                                            Object.class ) );
		definitions.put( "transactionManager",
		                 new ExposedBeanDefinition( mock( AcrossContextBeanRegistry.class ), "module",
		                                            "transactionManager", Object.class ) );
	}

	@Test
	public void renameWithoutRemovals() {
		Map<String, String> renames = new HashMap<String, String>();
		renames.put( "sessionFactory", "testSessionFactory" );

		ExposedBeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, false );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 2, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
		assertEquals( "transactionManager", definitions.get( "transactionManager" ).getPreferredBeanName() );
	}

	@Test
	public void renameWithRemovals() {
		Map<String, String> renames = new HashMap<String, String>();
		renames.put( "sessionFactory", "testSessionFactory" );

		ExposedBeanDefinitionTransformer transformer = new BeanRenameTransformer( renames, true );
		transformer.transformBeanDefinitions( definitions );

		assertEquals( 1, definitions.size() );
		assertEquals( "testSessionFactory", definitions.get( "sessionFactory" ).getPreferredBeanName() );
	}
}
