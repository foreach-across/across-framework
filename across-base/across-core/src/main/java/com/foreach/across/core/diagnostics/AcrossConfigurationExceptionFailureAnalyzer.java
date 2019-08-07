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

import com.foreach.across.core.AcrossConfigurationException;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.annotation.Order;

/**
 * @author Arne Vandamme
 * @see com.foreach.across.core.AcrossConfigurationException
 * @since 3.0.0
 */
@Order(1100)
class AcrossConfigurationExceptionFailureAnalyzer extends AbstractAcrossFailureAnalyzer<AcrossConfigurationException>
{
	@Override
	protected FailureAnalysis analyze( Throwable rootFailure, AcrossConfigurationException cause ) {
		return buildAnalysis( buildDescription( cause ), cause.getActionToTake(), cause.getCause() );
	}

	private String buildDescription( AcrossConfigurationException cause ) {
		if ( cause.getModuleBeingProcessed() != null ) {
			return String.format( "A configuration problem with module %s was detected.%n%s",
			                      cause.getModuleBeingProcessed(), cause.getMessage() );

		}
		return String.format( "A configuration problem was detected on the Across context.%n%s", cause.getMessage() );
	}
}
