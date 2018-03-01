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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Helper that builds the message with stacktrace.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public abstract class AbstractAcrossFailureAnalyzer<T extends Throwable> extends AbstractFailureAnalyzer<T>
{
	@SuppressWarnings("all")
	protected FailureAnalysis buildAnalysis( String description, String actions, Throwable cause ) {
		StringBuilder stacktrace = new StringBuilder();
		if ( cause != null ) {
			stacktrace.append( String.format( "%n%nStacktrace:%n%n" ) );
			stacktrace.append( String.format( "%s%n", ExceptionUtils.getStackTrace( cause ) ) );
		}

		if ( actions == null ) {
			return new FailureAnalysis( description + stacktrace.toString(), null, cause );
		}

		return new FailureAnalysis( description, actions + stacktrace.toString(), cause );
	}
}
