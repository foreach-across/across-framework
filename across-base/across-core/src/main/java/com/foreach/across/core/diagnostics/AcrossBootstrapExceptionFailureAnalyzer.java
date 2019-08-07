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

import com.foreach.across.core.context.bootstrap.AcrossBootstrapException;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.annotation.Order;

/**
 * @author Arne Vandamme
 * @see AcrossBootstrapException
 * @since 3.0.0
 */
@Order(1200)
class AcrossBootstrapExceptionFailureAnalyzer extends AbstractAcrossFailureAnalyzer<AcrossBootstrapException>
{
	@Override
	protected FailureAnalysis analyze( Throwable rootFailure, AcrossBootstrapException cause ) {
		return buildAnalysis( buildDescription( cause ), null, cause.getCause() );
	}

	private String buildDescription( AcrossBootstrapException cause ) {
		if ( cause.getModuleBeingProcessed() != null ) {
			return String.format( "An error occurred while bootstrapping module %s.", cause.getModuleBeingProcessed() );

		}
		return "An error occurred while bootstrapping the Across context.";
	}
}
