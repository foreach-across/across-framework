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
import com.foreach.across.core.AcrossException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossConfigurationExceptionFailureAnalyzer
{
	private AcrossConfigurationExceptionFailureAnalyzer analyzer = new AcrossConfigurationExceptionFailureAnalyzer();

	@Test
	public void configurationExceptionWithoutModule() {
		AcrossConfigurationException ae = new AcrossConfigurationException( "something happened" );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.isEqualTo( String.format( "A configuration problem was detected on the Across context.%nsomething happened" ) );
	}

	@Test
	public void configurationExceptionWithModule() {
		AcrossConfigurationException ae = new AcrossConfigurationException( "something happened" );
		ae.setModuleBeingProcessed( "MyModule" );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.isEqualTo( String.format( "A configuration problem with module MyModule was detected.%nsomething happened" ) );
	}

	@Test
	public void configurationExceptionWithAction() {
		AcrossConfigurationException ae = new AcrossConfigurationException( "something happened" );
		ae.setActionToTake( "change something" );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isEqualTo( "change something" );
	}

	@Test
	public void configurationExceptionWithAdditionalCause() {
		AcrossConfigurationException ae = new AcrossConfigurationException( "something happened", new AcrossException( "nested" ) );

		FailureAnalysis analysis = analyzer.analyze( null, ae );
		assertThat( analysis.getAction() ).isNull();
		assertThat( analysis.getDescription() )
				.startsWith( String.format( "A configuration problem was detected on the Across context.%nsomething happened" ) )
				.contains( "Stacktrace:" );
	}
}
