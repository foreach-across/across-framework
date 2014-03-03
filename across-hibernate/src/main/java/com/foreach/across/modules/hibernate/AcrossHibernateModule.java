package com.foreach.across.modules.hibernate;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Collection;
import java.util.Set;

/**
 * Activates hibernate support on all modules implementing HasHibernatePackageProvider
 * Will also activate Transactional support on the modules.
 */
public class AcrossHibernateModule extends AcrossModule
{
	/**
	 * @return Name of this module.  The spring bean should also be using this name.
	 */
	@Override
	public String getName() {
		return "AcrossHibernateModule";
	}

	/**
	 * @return Description of the content of this module.
	 */
	@Override
	public String getDescription() {
		return "Enables Hibernate support on the context.  Scans modules that are HibernatePackageProviders for this module.";
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {

	}

	/**
	 * Called when a context is preparing to bootstrap, but before the actual bootstrap happens.
	 * This is the last chance for a module to modify itself or its siblings before the actual
	 * bootstrapping will occur.
	 *
	 * @param modules AcrossModules in the order that they will be bootstrapped.
	 */
	@Override
	public void prepareForBootstrap( Collection<AcrossModule> modules ) {
		super.prepareForBootstrap( modules );
	}
}
