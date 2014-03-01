package com.foreach.across.test.modules.hibernatebase;

import java.util.Collection;

public interface HibernatePackageProvider
{
	Collection<String> getHibernatePackagesToScan();
}
