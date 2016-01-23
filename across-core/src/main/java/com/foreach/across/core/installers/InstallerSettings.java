/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.core.installers;

import org.springframework.util.Assert;

import java.util.*;

/**
 * Class representing the installer settings for a configured AcrossContext.
 */
public class InstallerSettings
{
	private InstallerAction defaultAction = InstallerAction.DISABLED;

	private InstallerActionResolver priorityActionResolver;

	private Map<String, InstallerAction> groupActions = new HashMap<>();
	private Map<String, InstallerAction> installerActions = new HashMap<>();

	public InstallerSettings() {
	}

	public InstallerSettings( InstallerAction defaultAction ) {
		this.defaultAction = defaultAction;
	}

	public void setDefaultAction( InstallerAction defaultAction ) {
		Assert.notNull( defaultAction, "Action should not be null." );
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

	/**
	 * Set the installer action for the names of the installer (usually the fully qualified class name).
	 */
	public void setActionForInstallers( InstallerAction action, String... installerNames ) {
		for ( String installerName : installerNames ) {
			installerActions.put( installerName, action );
		}
	}

	public void setActionForInstallers( InstallerAction action, Class<?>... installerClasses ) {
		for ( Class<?> installerClass : installerClasses ) {
			installerActions.put( InstallerMetaData.forClass( installerClass ).getName(), action );
		}
	}

	/**
	 * Checks if the installer should be run according to the settings.
	 *
	 * @param moduleName        Module trying to execute the installer.
	 * @param installerMetaData Installer metadata.
	 * @return Action that should be performed according to the settings.
	 */
	public InstallerAction shouldRun( String moduleName, InstallerMetaData installerMetaData ) {
		if ( installerMetaData == null ) {
			return InstallerAction.SKIP;
		}

		if ( defaultAction == InstallerAction.DISABLED ) {
			return InstallerAction.DISABLED;
		}

		InstallerAction action = null;

		if ( priorityActionResolver != null ) {
			Optional<InstallerAction> resolved = priorityActionResolver.resolve( moduleName, installerMetaData );

			if ( resolved.isPresent() ) {
				action = resolved.get();
			}
		}

		if ( action == null ) {
			action = installerActions.get( installerMetaData.getName() );

			if ( action == null ) {
				action = groupActions.get( installerMetaData.getGroup() );
			}

			if ( action == null ) {
				action = defaultAction;
			}
		}

		return action;
	}
}
