package com.foreach.across.core.installers;

/**
 * Determines when to execute an installer:
 * <ul>
 * <li>VersionDifferent: only if the @Installer.version() attribute is higher than the one currently installed</li>
 * <li>AlwaysRun: always execute this installer</li>
 * </ul>
 */
public enum InstallerRunCondition
{
	VersionDifferent,
	AlwaysRun
}
