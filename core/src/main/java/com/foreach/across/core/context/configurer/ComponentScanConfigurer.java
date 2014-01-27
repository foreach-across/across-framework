package com.foreach.across.core.context.configurer;

/**
 * Simple implementation for specifying packages an ApplicationContext should scan.
 */
public class ComponentScanConfigurer extends ApplicationContextConfigurerAdapter
{
	private String[] packages;

	public ComponentScanConfigurer( String... packages ) {
		this.packages = packages;
	}

	/**
	 * Return a set of packages that should be scanned for additional components.
	 *
	 * @return Array of package names.
	 */
	@Override
	public String[] componentScanPackages() {
		return packages;
	}
}
