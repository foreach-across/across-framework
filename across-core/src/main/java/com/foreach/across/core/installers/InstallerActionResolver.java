package com.foreach.across.core.installers;

/**
 * Implement to have a callback for resolving installer actions upon bootstrap.
 */
public interface InstallerActionResolver
{
	/**
	 * Resolve the action for an installer belonging to a particular group.  Used by
	 * the InstallerSettings to allow ad hoc user decision if installer should be run or not.
	 *
	 * @param group     Group the installer belongs to.
	 * @param installer Installer instance - not yet autowired.
	 * @return Action or null if it delegates the decision back to the InstallerSettings.
	 * @see com.foreach.across.core.installers.InstallerSettings
	 */
	InstallerAction resolve( String group, Object installer );
}
