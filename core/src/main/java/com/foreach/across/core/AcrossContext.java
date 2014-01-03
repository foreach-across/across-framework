package com.foreach.across.core;

public class AcrossContext
{
	private boolean allowInstallers;
	private boolean skipSchemaInstallers;
	private boolean onlyRegisterInstallers;

	public boolean isAllowInstallers() {
		return allowInstallers;
	}

	public void setAllowInstallers( boolean allowInstallers ) {
		this.allowInstallers = allowInstallers;
	}

	public boolean isSkipSchemaInstallers() {
		return skipSchemaInstallers;
	}

	public void setSkipSchemaInstallers( boolean skipSchemaInstallers ) {
		this.skipSchemaInstallers = skipSchemaInstallers;
	}

	public boolean isOnlyRegisterInstallers() {
		return onlyRegisterInstallers;
	}

	public void setOnlyRegisterInstallers( boolean onlyRegisterInstallers ) {
		this.onlyRegisterInstallers = onlyRegisterInstallers;
	}
}
