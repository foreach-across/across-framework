package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;

/**
 * <p>Will register the bean definitions as primary beans.  Optionally a list of the bean names
 * that should be set primary can be defined.</p>
 * <p>Note that this will in fact modify the original BeanDefinition.</p>
 */
public class PrimaryBeanTransformer implements ExposedBeanDefinitionTransformer
{
	private Collection<String> beanNames;

	public PrimaryBeanTransformer() {
	}

	public PrimaryBeanTransformer( Collection<String> beanNames ) {
		this.beanNames = beanNames;
	}

	public void setBeanNames( Collection<String> beanNames ) {
		Assert.notNull( beanNames );
		this.beanNames = beanNames;
	}

	public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
		for ( Map.Entry<String, ExposedBeanDefinition> definition : beanDefinitions.entrySet() ) {
			makePrimary( definition.getKey(), definition.getValue() );
		}
	}

	private void makePrimary( String beanName, ExposedBeanDefinition definition ) {
		if ( !definition.isPrimary() ) {
			if ( beanNames == null || beanNames.contains( beanName ) ) {
				definition.setPrimary( true );
			}
		}
	}
}
