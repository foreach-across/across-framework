package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBeanRenameTransformer implements ExposedBeanDefinitionTransformer
{
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	/**
	 * Modify the collection of ExposedBeanDefinitions.
	 *
	 * @param beanDefinitions Map of exposed bean definitions.
	 */
	public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
		List<String> removals = new LinkedList<>();

		for ( Map.Entry<String, ExposedBeanDefinition> definition : beanDefinitions.entrySet() ) {
			ExposedBeanDefinition exposed = definition.getValue();
			String name = rename( exposed.getPreferredBeanName(), exposed );

			if ( name == null ) {
				LOG.debug( "Removing exposed bean {} because preferredBeanName was null", definition.getKey() );
				removals.add( definition.getKey() );
			}
			else {
				exposed.setPreferredBeanName( name );
			}
		}

		for ( String removal : removals ) {
			beanDefinitions.remove( removal );
		}
	}

	protected abstract String rename( String beanName, ExposedBeanDefinition definition );
}
