package com.foreach.across.test.modules.hibernate1;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.modules.hibernatebase.HibernatePackageProvider;

import java.util.Arrays;
import java.util.Collection;

public class Hibernate1Module extends AcrossModule implements HibernatePackageProvider
{
	@Override
	public String getName() {
		return "Hibernate1Module";
	}

	@Override
	public String getDescription() {
		return null;
	}

	public Collection<String> getHibernatePackagesToScan() {
		return Arrays.asList( "com.foreach.across.test.modules.hibernate1" );
	}
}
