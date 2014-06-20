package com.foreach.across.modules.ehcache;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Set;

@AcrossRole(AcrossModuleRole.INFRASTRUCTURE)
@AcrossDepends(optional = "DebugWebModule")
public class EhcacheModule extends AcrossModule
{
	private Resource configLocation = new ClassPathResource( "ehcache.xml" );

	public Resource getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation( Resource configLocation ) {
		this.configLocation = configLocation;
	}

	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return "EhcacheModule";
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return "Registers an Ehcache cachemanager and ensures all other modules use it as well.";
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		super.registerDefaultApplicationContextConfigurers( contextConfigurers );
	}
}
