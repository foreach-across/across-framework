package com.foreach.across.modules.hibernate.provider;

public class HibernatePackageProviderAdapter implements HibernatePackageProvider
{
	public String[] getPackagesToScan() {
		return new String[0];
	}

	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[0];
	}

	public String[] getMappingResources() {
		return new String[0];
	}
}
