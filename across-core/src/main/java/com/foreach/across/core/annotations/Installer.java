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
	 * Optional name of the installer.  If the name is empty, the fully qualified class name of the installer
	 * will be used instead.  Manually providing a name allows for greater flexibility with renaming or moving
	 * the actual installer class.
	 *
	 * @return Name of the installer or empty string if the class name should be used.
	 */
	String name() default "";

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
