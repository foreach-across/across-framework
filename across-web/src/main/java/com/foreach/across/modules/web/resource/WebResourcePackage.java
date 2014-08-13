package com.foreach.across.modules.web.resource;

public interface WebResourcePackage
{
	/**
	 * Install the resources contained in the package in the registry.
	 *
	 * @param registry Registry to which to add the resources.
	 */
	void install( WebResourceRegistry registry );

	/**
	 * Uninstall the package from the given registry.
	 *
	 * @param registry Registry where the package is supposedly installed.
	 */
	void uninstall( WebResourceRegistry registry );
}
