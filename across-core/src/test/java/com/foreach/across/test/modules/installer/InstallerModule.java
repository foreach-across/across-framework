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

package com.foreach.across.test.modules.installer;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.modules.installer.installers.AlwaysRunAfterModuleBootstrapInstaller;
import com.foreach.across.test.modules.installer.installers.AlwaysRunBeforeContextBootstrapInstaller;
import com.foreach.across.test.modules.installer.installers.VersionBasedInstaller;

public class InstallerModule extends AcrossModule
{
	@Override
	public String getName() {
		return "InstallerModule";
	}

	@Override
	public String getDescription() {
		return "Test module for installers.";
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				AlwaysRunBeforeContextBootstrapInstaller.class,
				AlwaysRunAfterModuleBootstrapInstaller.class,
				VersionBasedInstaller.class
		};
	}
}
