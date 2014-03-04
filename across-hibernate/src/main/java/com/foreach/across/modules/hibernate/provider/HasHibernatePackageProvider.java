package com.foreach.across.modules.hibernate.provider;

import com.foreach.across.modules.hibernate.AcrossHibernateModule;

/**
 * Interface to be used on AcrossModule instances to indicate they provide Hibernate packages.
 */
public interface HasHibernatePackageProvider
{
	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @param hibernateModule AcrossHibernateModule that is requesting packages.
	 * @return HibernatePackageProvider instance.
	 */
	HibernatePackageProvider getHibernatePackageProvider( AcrossHibernateModule hibernateModule );
}
