package com.foreach.across.modules.hibernate.provider;

public class AnnotatedClassProvider extends HibernatePackageProviderAdapter
{
	private Class<?>[] annotatedClasses;

	public AnnotatedClassProvider( Class<?>... annotatedClasses ) {
		this.annotatedClasses = annotatedClasses;
	}

	@Override
	public Class<?>[] getAnnotatedClasses() {
		return annotatedClasses;
	}
}
