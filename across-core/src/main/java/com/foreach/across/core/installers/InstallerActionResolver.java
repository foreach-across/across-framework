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

import java.util.Optional;

/**
 * Implement to have a callback for resolving installer actions upon bootstrap.
 *
 * When an {@link com.foreach.across.core.annotations.Installer} annotated class implements this interface,
 * the {@link #resolve(String, InstallerMetaData)} method will be called if the pre-determined action is
 * {@link InstallerAction#EXECUTE}.
 *
 * @see InstallerSettings
 */
public interface InstallerActionResolver
{
	/**
	 * Resolve the action for a particular installer in a module.
	 *
	 * @param moduleName        Module that is running the installer.
	 * @param installerMetaData Installer metadata.
	 * @return Action or empty if it delegates the decision back to the InstallerSettings.
	 * @see com.foreach.across.core.installers.InstallerSettings
	 */
	Optional<InstallerAction> resolve( String moduleName, InstallerMetaData installerMetaData );
}
