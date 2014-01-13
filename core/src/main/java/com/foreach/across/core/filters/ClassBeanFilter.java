package com.foreach.across.core.filters;

import org.apache.commons.lang3.StringUtils;

/**
 * Filters on any number of specific classes.
 */
public class ClassBeanFilter extends AbstractNameBeanFilter<Class>
{
	public ClassBeanFilter( Class... allowedClasses ) {
		super( allowedClasses );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean matches( Class beanClass, Class expected ) {
		return expected.isAssignableFrom( beanClass );
	}

	@Override
	protected boolean matches( String beanClassName, Class expected ) {
		return StringUtils.equalsIgnoreCase( beanClassName, expected.getName() );
	}
}
