package com.foreach.across.modules.hibernate.provider;

import java.util.Collections;
import java.util.Map;

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

	@Override
	public Map<String, String> getTableAliases() {
		return Collections.emptyMap();
	}
}
