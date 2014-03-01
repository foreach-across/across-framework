package com.foreach.across.test.modules.hibernatebase;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.TransactionManagementConfigurer;

import java.util.Collection;
import java.util.HashSet;

/**
 * Example module that will scan all other modules to see if they provide Hibernate managed entities.
 * If they do, the packages to scan will be added and Transaction support will be enabled in the module.
 */
public class HibernateBaseModule extends AcrossModule
{
	private final Collection<String> packagesToScan = new HashSet<String>();

	public Collection<String> getPackagesToScan() {
		return packagesToScan;
	}

	@Override
	public String getName() {
		return "HibernateBaseModule";
	}

	@Override
	public String getDescription() {
		return "Creates session factory and enables transaction support in all modules.";
	}

	@Override
	public void prepareForBootstrap( Collection<AcrossModule> modules ) {
		for ( AcrossModule module : modules ) {
			if ( module instanceof HibernatePackageProvider ) {
				packagesToScan.addAll( ( (HibernatePackageProvider) module ).getHibernatePackagesToScan() );
				module.addApplicationContextConfigurer( new TransactionManagementConfigurer() );
			}
		}
	}
}
