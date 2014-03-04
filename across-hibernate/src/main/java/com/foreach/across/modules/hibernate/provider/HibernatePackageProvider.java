package com.foreach.across.modules.hibernate.provider;

public interface HibernatePackageProvider
{
	String[] getPackagesToScan();

	Class<?>[] getAnnotatedClasses();

	String[] getMappingResources();
}
