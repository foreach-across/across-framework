package com.foreach.across.modules.ehcache;

import com.foreach.across.core.AcrossModule;

public class EhCacheModule extends AcrossModule
{
	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return "EhCacheModule";
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return "Registers an EhCache cachemanagers and ensures all other modules use it as well.";
	}
}
