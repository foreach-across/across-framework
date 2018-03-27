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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossException;

/**
 * Specialization of {@link AcrossException} for wrapping non-Across exceptions thrown during the actual bootstrapping of the context.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.diagnostics.AcrossBootstrapExceptionFailureAnalyzer
 * @since 3.0.0
 */
public class AcrossBootstrapException extends AcrossException
{
	AcrossBootstrapException( Throwable cause ) {
		super( cause );
	}

	@Override
	public String getMessage() {
		if ( getModuleBeingProcessed() != null ) {
			return "Across module bootstrap failed: " + getModuleBeingProcessed();
		}
		return "Across context bootstrap failed";
	}
}
