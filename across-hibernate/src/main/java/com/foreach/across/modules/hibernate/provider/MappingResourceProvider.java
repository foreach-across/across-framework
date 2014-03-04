package com.foreach.across.modules.hibernate.provider;

public class MappingResourceProvider extends HibernatePackageProviderAdapter
{
	private String[] mappingResources;

	public MappingResourceProvider( String... mappingResources ) {
		this.mappingResources = mappingResources;
	}

	@Override
	public String[] getMappingResources() {
		return mappingResources;
	}
}
