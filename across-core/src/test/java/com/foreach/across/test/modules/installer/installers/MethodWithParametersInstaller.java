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
package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.test.installers.TestAcrossInstallerRegistry;

/**
 * @author Steven Gentens
 * @since 3.0.0
 */
@Installer(description = "Installer that will always run and has 2 installer methods.",
		runCondition = InstallerRunCondition.AlwaysRun)
public class MethodWithParametersInstaller extends TestInstaller
{
	@InstallerMethod
	public void method( TestAcrossInstallerRegistry.SomeBean someBean ) {
		// Overrides the parent method
		if ( someBean != null ) {
			EXECUTED.add( someBean.getClass() );
		}
	}

	@InstallerMethod(required = false)
	public void anotherMethod( TestAcrossInstallerRegistry.SomeOtherBean someOtherBean, TestAcrossInstallerRegistry.AnotherBean anotherBean ) {
		// Overrides the parent method
		if ( someOtherBean != null ) {
			EXECUTED.add( someOtherBean.getClass() );
		}
		if ( anotherBean != null ) {
			EXECUTED.add( anotherBean.getClass() );
		}

	}

}
