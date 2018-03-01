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

import com.foreach.across.core.installers.AcrossInstallerException;
import com.foreach.across.core.installers.InstallerMetaData;
import com.foreach.across.core.installers.InstallerRunCondition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.annotation.Order;

/**
 * @author Arne Vandamme
 * @see com.foreach.across.core.installers.AcrossInstallerException
 * @since 3.0.0
 */
@Order(1000)
class AcrossInstallerExceptionFailureAnalyzer extends AbstractAcrossFailureAnalyzer<AcrossInstallerException>
{
	@Override
	protected FailureAnalysis analyze( Throwable rootFailure, AcrossInstallerException cause ) {
		return buildAnalysis( buildDescription( cause ), null, cause.getCause() );
	}

	private String buildDescription( AcrossInstallerException cause ) {
		StringBuilder description = new StringBuilder();
		description.append( String.format( "A error occurred when executing an installer for module MyModule:%n" ) );
		InstallerMetaData installerMetaData = cause.getInstallerMetaData();
		description.append( String.format( " - Installer name: %s%n", installerMetaData.getName() ) );
		description.append( String.format( " - Description: %s%n", installerMetaData.getDescription() ) );
		description.append( String.format( " - Installer class: %s%n", cause.getInstallerClass().getName() ) );
		if ( !StringUtils.isEmpty( installerMetaData.getGroup() ) ) {
			description.append( String.format( " - Installer group: %s%n", installerMetaData.getGroup() ) );
		}
		description.append( String.format( " - Installer phase: %s%n", installerMetaData.getInstallerPhase() ) );
		description.append(
				String.format( " - Run condition: %s%s%n",
				               installerMetaData.getRunCondition(),
				               installerMetaData.getRunCondition() == InstallerRunCondition.VersionDifferent
						               ? String.format( " (version: %s)", installerMetaData.getVersion() ) : ""
				)
		);
		description.append( String.format( " - Installer method: %s", cause.getInstallerMethod() ) );

		return description.toString();
	}
}
