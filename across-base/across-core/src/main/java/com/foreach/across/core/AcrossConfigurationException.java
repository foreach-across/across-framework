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
 * Specialization of {@link AcrossException} used to indicate a configuration error.
 * Might be thrown before (or during) bootstrap of the Across context, when validating configuration.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.diagnostics.AcrossConfigurationExceptionFailureAnalyzer
 * @since 3.0.0
 */
public class AcrossConfigurationException extends AcrossException
{
	@Getter
	@Setter
	private String actionToTake;

	public AcrossConfigurationException( String message ) {
		super( message );
	}

	public AcrossConfigurationException( String message, String actionToTake ) {
		this( message );
		this.actionToTake = actionToTake;
	}

	public AcrossConfigurationException( String message, Throwable cause ) {
		super( message, cause );
	}
}
