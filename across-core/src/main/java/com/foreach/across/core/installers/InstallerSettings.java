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
	private InstallerAction defaultAction = InstallerAction.DISABLED;

	private InstallerActionResolver priorityActionResolver;

	private Map<String, InstallerAction> groupActions = new HashMap<>();
	private Map<String, InstallerAction> installerActions = new HashMap<>();

	public void setDefaultAction( InstallerAction defaultAction ) {
		this.defaultAction = defaultAction;
	}

	public InstallerAction getDefaultAction() {
		return defaultAction;
	}

	public void setGroupActions( Map<String, InstallerAction> groupActions ) {
		this.groupActions = groupActions;
	}

	public void setInstallerActions( Map<String, InstallerAction> installerActions ) {
		this.installerActions = installerActions;
	}

	public Map<String, InstallerAction> getGroupActions() {
		return groupActions;
	}

	public Map<String, InstallerAction> getInstallerActions() {
		return installerActions;
	}

	/**
	 * @return The priority action resolver attached to the settings (default is null).
	 */
	public InstallerActionResolver getPriorityActionResolver() {
		return priorityActionResolver;
	}

	/**
	 * Sets a priority installer action resolver.  If one is set, that result will be used for the installer, unless:
	 * <ul>
	 * <li>the result from the resolver is null - in which case the normal flow will execute</li>
	 * <li>the default action is DISABLED - in which case the resolver will not be called</li>
	 * </ul>
	 *
	 * @param priorityActionResolver InstallerActionResolver instance.
	 */
	public void setPriorityActionResolver( InstallerActionResolver priorityActionResolver ) {
		this.priorityActionResolver = priorityActionResolver;
	}

	public void setActionForInstallerGroups( InstallerAction action, String... groups ) {
		setActionForInstallerGroups( action, Arrays.asList( groups ) );
	}

	public void setActionForInstallerGroups( InstallerAction action, Collection<String> groups ) {
		for ( String group : groups ) {
			Assert.notNull( group, "Cant put an action on a null group." );
			groupActions.put( group, action );
		}
	}

	public void setActionForInstallers( InstallerAction action, String... installers ) {
		setActionForInstallers( action, Arrays.asList( installers ) );
	}

	public void setActionForInstallers( InstallerAction action, Class... installerClasses ) {
		setActionForInstallers( action, Arrays.asList( installerClasses ) );
	}

	public void setActionForInstallers( InstallerAction action, Object... installers ) {
		setActionForInstallers( action, Arrays.asList( installers ) );
	}

	/**
	 * Sets the action to apply to the a collection of installers.
	 *
	 * @param action     Action to apply to these installers.
	 * @param installers Installer name (String), type (Class) or instance.
	 */
	public void setActionForInstallers( InstallerAction action, Collection installers ) {
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
	public InstallerAction shouldRun( String installerGroup, Object installer ) {
		if ( installer == null ) {
			return InstallerAction.SKIP;
		}

		if ( defaultAction == InstallerAction.DISABLED ) {
			return InstallerAction.DISABLED;
		}

		InstallerAction action = null;

		if ( priorityActionResolver != null ) {
			action = priorityActionResolver.resolve( installerGroup, installer );
		}

		if ( action == null ) {
			String installerId = installer.getClass().getCanonicalName();

			action = installerActions.get( installerId );

			if ( action == null ) {
				action = groupActions.get( installerGroup );
			}

			if ( action == null ) {
				action = defaultAction;
			}
		}

		return action;
	}
}
