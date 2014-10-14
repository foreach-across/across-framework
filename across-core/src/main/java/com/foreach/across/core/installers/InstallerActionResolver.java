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
