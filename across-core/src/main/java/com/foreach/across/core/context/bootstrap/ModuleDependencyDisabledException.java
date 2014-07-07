package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossException;

public class ModuleDependencyDisabledException extends AcrossException
{
	private final String moduleName, dependencyName;

	public ModuleDependencyDisabledException( String moduleName, String dependencyName ) {
		super( "Unable to bootstrap AcrossContext as module " + moduleName + " requires module " + dependencyName
				       + ".  Module " + dependencyName + " is not present in the context." );

		this.moduleName = moduleName;
		this.dependencyName = dependencyName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getDependencyName() {
		return dependencyName;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ModuleDependencyDisabledException that = (ModuleDependencyDisabledException) o;

		if ( dependencyName != null ? !dependencyName.equals( that.dependencyName ) : that.dependencyName != null ) {
			return false;
		}
		if ( moduleName != null ? !moduleName.equals( that.moduleName ) : that.moduleName != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = moduleName != null ? moduleName.hashCode() : 0;
		result = 31 * result + ( dependencyName != null ? dependencyName.hashCode() : 0 );
		return result;
	}
}
