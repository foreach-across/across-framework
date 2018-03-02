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

package com.foreach.across.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Generic base class for exceptions thrown by Across processing.
 *
 * @see com.foreach.across.core.diagnostics.AcrossExceptionFailureAnalyzer
 */
public class AcrossException extends RuntimeException
{
	/**
	 * Name of the module that was being processed. Depending on the exception type
	 * this might mean the module was being bootstrapped or something happened during
	 * configuration of this particular module.
	 */
	@Getter
	@Setter
	private String moduleBeingProcessed;

	public AcrossException( String message ) {
		super( message );
	}

	public AcrossException( String message, Throwable cause ) {
		super( message, cause );
	}

	protected AcrossException( Throwable cause ) {
		super( cause );
	}
}
