package com.foreach.across.test.modules.hibernate2;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.modules.hibernatebase.HibernatePackageProvider;

import java.util.Arrays;
import java.util.Collection;

public class Hibernate2Module extends AcrossModule implements HibernatePackageProvider
{
	@Override
	public String getName() {
		return "Hibernate2Module";
	}

	@Override
	public String getDescription() {
		return null;
	}

	public Collection<String> getHibernatePackagesToScan() {
		return Arrays.asList( "com.foreach.across.test.modules.hibernate2" );
	}
}
