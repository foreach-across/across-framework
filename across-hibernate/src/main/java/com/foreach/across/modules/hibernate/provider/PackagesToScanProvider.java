package com.foreach.across.modules.hibernate.provider;

public class PackagesToScanProvider extends HibernatePackageProviderAdapter
{
	private String[] packagesToScan;

	public PackagesToScanProvider( String... packagesToScan ) {
		this.packagesToScan = packagesToScan;
	}

	@Override
	public String[] getPackagesToScan() {
		return packagesToScan;
	}
}
