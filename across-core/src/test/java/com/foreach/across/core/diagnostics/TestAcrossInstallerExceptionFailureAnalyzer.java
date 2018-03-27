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
package com.foreach.across.core.diagnostics;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerGroup;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.AcrossInstallerException;
import com.foreach.across.core.installers.InstallerMetaData;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossInstallerExceptionFailureAnalyzer
{
	private static final Method INSTALLER_METHOD = MethodUtils.getMatchingMethod( InstallerGroupInstaller.class, "myInstallerMethod" );

	private AcrossInstallerExceptionFailureAnalyzer analyzer = new AcrossInstallerExceptionFailureAnalyzer();

	@Test
	public void installerMetadataPrinting() {
		InstallerMetaData installerMetaData = InstallerMetaData.forClass( InstallerGroupInstaller.class );

		AcrossInstallerException ae = new AcrossInstallerException( "MyModule", installerMetaData, 123L, INSTALLER_METHOD, new RuntimeException( "boom" ) );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.startsWith( String.format( "A error occurred when executing an installer for module MyModule:%n" +
						                            " - Installer name: CustomInstallerName%n" +
						                            " - Description: with group%n" +
						                            " - Installer class: %s%n" +
						                            " - Installer group: someGroup%n" +
						                            " - Installer phase: AfterModuleBootstrap%n" +
						                            " - Run condition: VersionDifferent (version: -2)%n" +
						                            " - Installer method: %s",
				                            Long.class.getName(),
				                            INSTALLER_METHOD.toString() ) );
	}

	@SuppressWarnings("unused")
	@InstallerGroup("someGroup")
	@Installer(
			description = "with group",
			runCondition = InstallerRunCondition.VersionDifferent,
			version = -2,
			phase = InstallerPhase.AfterModuleBootstrap,
			name = "CustomInstallerName"
	)
	private static class InstallerGroupInstaller
	{
		@InstallerMethod
		void myInstallerMethod() {
		}
	}
}
