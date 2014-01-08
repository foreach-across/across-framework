package com.foreach.across.core;

import java.lang.annotation.*;

/**
 * Used to indicate installer metadata..
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Installer
{
	/**
	 * @return Descriptive text of what will be installed.
	 */
	public String description();

	/**
	 * @return Version number of the installer.  Increment to enforce a new run of the same installer.  Default value: 1
	 */
	public int version() default 1;

	/**
	 * @return Condition when this installer should run.  Default value: VersionDifferent
	 */
	public InstallerRunCondition runCondition() default InstallerRunCondition.VersionDifferent;

	/**
	 * @return When should this installer run during the context bootstrap.  Default value: BeforeContextBootstrap
	 */
	public InstallerPhase phase() default InstallerPhase.BeforeContextBootstrap;
}
