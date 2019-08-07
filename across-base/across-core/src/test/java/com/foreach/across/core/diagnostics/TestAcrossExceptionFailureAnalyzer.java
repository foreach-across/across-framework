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
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossExceptionFailureAnalyzer
{
	private AcrossExceptionFailureAnalyzer analyzer = new AcrossExceptionFailureAnalyzer();

	@Test
	public void generalExceptionWithoutModule() {
		AcrossException ae = new AcrossException( "something happened" );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.startsWith( String.format("An error occurred when starting the Across context.%nsomething happened") );
	}

	@Test
	public void generalExceptionWithModule() {
		AcrossException ae = new AcrossException( "something happened" );
		ae.setModuleBeingProcessed( "MyModule" );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.startsWith( String.format("There was an error with module MyModule when starting the Across context.%nsomething happened") );
	}
}
