package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Transformer that allows defining a new name for specific bean names.
 * Beans with an undefined name (without other name specified) can optionally be removed from the set.
 */
public class BeanRenameTransformer extends AbstractBeanRenameTransformer
{
	private final Map<String, String> renameMap;
	private final boolean removeUndefined;

	public BeanRenameTransformer( Map<String, String> renameMap, boolean removeUndefined ) {
		Assert.notNull( renameMap );
		this.renameMap = renameMap;
		this.removeUndefined = removeUndefined;
	}

	@Override
	protected String rename( String beanName, ExposedBeanDefinition definition  ) {
		String name = renameMap.get( beanName );

		return name != null ? name : ( removeUndefined ? null : beanName );
	}
}
