package com.foreach.across.core.context.bootstrap;

public class CyclicModuleDependencyException extends RuntimeException
{
	private final String module;

	public CyclicModuleDependencyException( String module ) {
		super( "Unable to determine legal module bootstrap order, possible cyclic dependency on module " + module );
		this.module = module;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		CyclicModuleDependencyException that = (CyclicModuleDependencyException) o;

		if ( module != null ? !module.equals( that.module ) : that.module != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return module != null ? module.hashCode() : 0;
	}
}
