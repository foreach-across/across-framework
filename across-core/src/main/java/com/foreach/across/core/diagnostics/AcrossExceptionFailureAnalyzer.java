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

import com.foreach.across.core.AcrossException;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.annotation.Order;

/**
 * Generic failure analyzer for an {@link AcrossException}.
 * Considers the exception to be a general configuration exception and prints out the message,
 * without any specific actions.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Order(1500)
class AcrossExceptionFailureAnalyzer extends AbstractAcrossFailureAnalyzer<AcrossException>
{
	@Override
	protected FailureAnalysis analyze( Throwable rootFailure, AcrossException cause ) {
		return buildAnalysis( buildDescription( cause ), null, cause );
	}

	private String buildDescription( AcrossException cause ) {
		if ( cause.getModuleBeingProcessed() != null ) {
			return String.format( "There was an error with module %s when starting the Across context.%n%s",
			                      cause.getModuleBeingProcessed(), cause.getMessage() );

		}
		return String.format( "An error occurred when starting the Across context.%n%s", cause.getMessage() );
	}
}
