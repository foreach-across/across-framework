package com.foreach.across.modules.hibernate;

/**
 * Interface to be used on AcrossModule instances to indicate they provide Hibernate packages.
 */
public interface HasHibernatePackageProvider
{
	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @return HibernatePackageProvider instance.
	 */
	HibernatePackageProvider getHibernatePackageProvider();
}
