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
 * Central repository interface for managing the installer execution tracking records.
 *
 * @author Arne Vandamme
 */
public interface AcrossInstallerRepository
{
	/**
	 * Rename an installer globally.  This will change the installer name for every module.
	 *
	 * @param oldInstallerName to be renamed
	 * @param newInstallerName new name to use
	 * @return number of installers updated
	 */
	int renameInstaller( String oldInstallerName, String newInstallerName );

	/**
	 * Rename an installer for a particular module.  If other modules have an installer with the
	 * same name, they will be untouched.
	 *
	 * @param oldInstallerName to be renamed
	 * @param newInstallerName new name to use
	 * @param moduleName       module for which to rename the installer
	 * @return {@code true} if installer was registered and has been renamed
	 */
	boolean renameInstallerForModule( String oldInstallerName, String newInstallerName, String moduleName );

	/**
	 * Get the installed version of a particular installer.  If the installer has not yet been registered
	 * (never executed), {@code -1} will be returned.
	 *
	 * @param moduleName    name of the module
	 * @param installerName name of the installer
	 * @return version or -1 if not found
	 */
	int getInstalledVersion( String moduleName, String installerName );

	/**
	 * Registers the installer metadata for a particular module.  This effectively registers the installer
	 * as having been executed.  If there was already an installe record, it will be updated.
	 *
	 * @param moduleName        name of the module
	 * @param installerMetaData metadata of the installer to register
	 */
	void setInstalled( String moduleName, InstallerMetaData installerMetaData );
}
