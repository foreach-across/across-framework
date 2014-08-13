package com.foreach.across.core.annotations;

import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;

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
	String description();

	/**
	 * @return Version number of the installer.  Increment to enforce a new run of the same installer.  Default value: 1
	 */
	int version() default 1;

	/**
	 * @return Condition when this installer should run.  Default value: VersionDifferent
	 */
	InstallerRunCondition runCondition() default InstallerRunCondition.VersionDifferent;

	/**
	 * @return When should this installer run during the context bootstrap.  Default value: BeforeContextBootstrap
	 */
	InstallerPhase phase() default InstallerPhase.BeforeContextBootstrap;
}
