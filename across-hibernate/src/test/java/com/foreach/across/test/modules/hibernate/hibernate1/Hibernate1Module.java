package com.foreach.across.test.modules.hibernate.hibernate1;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.HasHibernatePackageProvider;
import com.foreach.across.modules.hibernate.provider.HibernatePackageProvider;
import com.foreach.across.modules.hibernate.provider.PackagesToScanProvider;

public class Hibernate1Module extends AcrossModule implements HasHibernatePackageProvider
{
	@Override
	public String getName() {
		return "Hibernate1Module";
	}

	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @param hibernateModule AcrossHibernateModule that is requesting packages.
	 * @return HibernatePackageProvider instance.
	 */
	public HibernatePackageProvider getHibernatePackageProvider( AcrossHibernateModule hibernateModule ) {
		return new PackagesToScanProvider( "com.foreach.across.test.modules.hibernate.hibernate1" );
	}
}
