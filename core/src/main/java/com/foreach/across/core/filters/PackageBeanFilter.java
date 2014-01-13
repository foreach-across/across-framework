package com.foreach.across.core.filters;

import org.apache.commons.lang3.StringUtils;

/**
 * Applies to all classes that are in one of the given packages.
 */
public class PackageBeanFilter extends AbstractNameBeanFilter<String>
{
	public PackageBeanFilter( String... allowedPackages ) {
		super( allowedPackages );
	}

	public String[] getAllowedPackages() {
		return getAllowedItems();
	}

	public void setAllowedPackages( String[] allowedPackages ) {
		setAllowedItems( allowedPackages );
	}

	@Override
	protected boolean matches( Class beanClass, String expectedPackage ) {
		return matches( beanClass.getName(), expectedPackage );
	}

	@Override
	protected boolean matches( String beanClassName, String expectedPackage ) {
		return StringUtils.startsWith( beanClassName, expectedPackage );
	}
}
