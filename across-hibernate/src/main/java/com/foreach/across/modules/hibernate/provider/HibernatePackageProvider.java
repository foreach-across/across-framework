package com.foreach.across.modules.hibernate.provider;

import java.util.Map;

public interface HibernatePackageProvider
{
	String[] getPackagesToScan();

	Class<?>[] getAnnotatedClasses();

	String[] getMappingResources();

	Map<String, String> getTableAliases();
}
