package com.foreach.across.core.installers;

public enum InstallerAction
{
	/**
	 * Execute if the regular conditions apply (this is the normal flow for running installers).
	 */
	EXECUTE,

	/**
	 * Execute no matter what the installer conditions are.
	 */
	FORCE,

	/**
	 * Register the installer as if it has run.
	 */
	REGISTER,

	/**
	 * Skip the installer.
	 */
	SKIP,

	/**
	 * Disabled.  Basically the same as skip, but if disabled is the default action,
	 * all more specific settings will be forcibly overruled.  No installers in this
	 * set will execute.
	 */
	DISABLED
}
