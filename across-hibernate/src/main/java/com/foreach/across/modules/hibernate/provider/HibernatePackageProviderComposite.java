package com.foreach.across.modules.hibernate.provider;

import java.util.*;

public class HibernatePackageProviderComposite implements HibernatePackageProvider
{
	private HibernatePackageProvider[] providers;

	public HibernatePackageProviderComposite( HibernatePackageProvider... providers ) {
		this.providers = providers;
	}

	@Override
	public String[] getPackagesToScan() {
		Collection<String> packagesToScan = new HashSet<String>();
		for ( HibernatePackageProvider provider : providers ) {
			packagesToScan.addAll( Arrays.asList( provider.getPackagesToScan() ) );
		}

		return packagesToScan.toArray( new String[packagesToScan.size()] );
	}

	@Override
	public Class<?>[] getAnnotatedClasses() {
		Collection<Class<?>> annotatedClasses = new HashSet<Class<?>>();
		for ( HibernatePackageProvider provider : providers ) {
			annotatedClasses.addAll( Arrays.asList( provider.getAnnotatedClasses() ) );
		}

		return annotatedClasses.toArray( new Class<?>[annotatedClasses.size()] );
	}

	@Override
	public String[] getMappingResources() {
		Collection<String> mappingResources = new HashSet<String>();
		for ( HibernatePackageProvider provider : providers ) {
			mappingResources.addAll( Arrays.asList( provider.getMappingResources() ) );
		}

		return mappingResources.toArray( new String[mappingResources.size()] );
	}

	@Override
	public Map<String, String> getTableAliases() {
		Map<String, String> tableAliases = new HashMap<>();
		for ( HibernatePackageProvider provider : providers ) {
			tableAliases.putAll( provider.getTableAliases() );
		}

		return tableAliases;
	}
}
