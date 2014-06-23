package com.foreach.across.core.installers;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the installer settings for a configured AcrossContext.
 */
public class InstallerSettings
{
	public static enum Action
	{
		/**
		 * Execute if the regular conditions apply (this is the default when executing installers).
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

	private Action defaultAction = Action.DISABLED;

	private Map<String, Action> groupActions = new HashMap<>();
	private Map<String, Action> installerActions = new HashMap<>();

	public void setDefaultAction( Action defaultAction ) {
		this.defaultAction = defaultAction;
	}

	public Action getDefaultAction() {
		return defaultAction;
	}

	public void setGroupActions( Map<String, Action> groupActions ) {
		this.groupActions = groupActions;
	}

	public void setInstallerActions( Map<String, Action> installerActions ) {
		this.installerActions = installerActions;
	}

	public Map<String, Action> getGroupActions() {
		return groupActions;
	}

	public Map<String, Action> getInstallerActions() {
		return installerActions;
	}

	public void setActionForInstallerGroups( Action action, String... groups ) {
		setActionForInstallerGroups( action, Arrays.asList( groups ) );
	}

	public void setActionForInstallerGroups( Action action, Collection<String> groups ) {
		for ( String group : groups ) {
			Assert.notNull( group, "Cant put an action on a null group." );
			groupActions.put( group, action );
		}
	}

	public void setActionForInstallers( Action action, String... installers ) {
		setActionForInstallers( action, Arrays.asList( installers ) );
	}

	public void setActionForInstallers( Action action, Class... installerClasses ) {
		setActionForInstallers( action, Arrays.asList( installerClasses ) );
	}

	public void setActionForInstallers( Action action, Object... installers ) {
		setActionForInstallers( action, Arrays.asList( installers ) );
	}

	/**
	 * Sets the action to apply to the a collection of installers.
	 *
	 * @param action     Action to apply to these installers.
	 * @param installers Installer name (String), type (Class) or instance.
	 */
	public void setActionForInstallers( Action action, Collection installers ) {
		for ( Object installer : installers ) {
			Assert.notNull( installer, "Installer should not be null." );

			if ( installer instanceof String ) {
				installerActions.put( (String) installer, action );
			}
			else if ( installer instanceof Class ) {
				installerActions.put( ( (Class) installer ).getCanonicalName(), action );
			}
			else {
				installerActions.put( installer.getClass().getCanonicalName(), action );
			}
		}
	}

	/**
	 * Checks if the installer should be run according to the settings.
	 *
	 * @param installerGroup Installer group the installer belongs to.
	 * @param installer      Installer instance.
	 * @return Action that should be performed according to the settings.
	 */
	public Action shouldRun( String installerGroup, Object installer ) {
		if ( installer == null ) {
			return Action.SKIP;
		}

		String installerId = installer.getClass().getCanonicalName();

		Action action = installerActions.get( installerId );

		if ( action != null ) {
			return action;
		}

		return defaultAction;
	}
}
