/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.module;

import com.foreach.across.core.context.AcrossModuleRole;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
class TestAcrossModuleRole
{
	@Test
	void infrastructure() {
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( null ) ).isEqualTo( 10L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( 0 ) ).isEqualTo( 10L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( Ordered.HIGHEST_PRECEDENCE ) )
				.isEqualTo( 10L * Integer.MAX_VALUE + Ordered.HIGHEST_PRECEDENCE );
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( Ordered.LOWEST_PRECEDENCE ) ).isEqualTo( 10L * Integer.MAX_VALUE + Ordered.LOWEST_PRECEDENCE );
	}

	@Test
	void application() {
		assertThat( AcrossModuleRole.APPLICATION.asPriority( null ) ).isEqualTo( 20L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.APPLICATION.asPriority( 0 ) ).isEqualTo( 20L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.APPLICATION.asPriority( Ordered.HIGHEST_PRECEDENCE ) ).isEqualTo( 20L * Integer.MAX_VALUE + Ordered.HIGHEST_PRECEDENCE );
		assertThat( AcrossModuleRole.APPLICATION.asPriority( Ordered.LOWEST_PRECEDENCE ) ).isEqualTo( 20L * Integer.MAX_VALUE + Ordered.LOWEST_PRECEDENCE );
	}

	@Test
	void postProcessor() {
		assertThat( AcrossModuleRole.POSTPROCESSOR.asPriority( null ) ).isEqualTo( 30L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.POSTPROCESSOR.asPriority( 0 ) ).isEqualTo( 30L * Integer.MAX_VALUE );
		assertThat( AcrossModuleRole.POSTPROCESSOR.asPriority( Ordered.HIGHEST_PRECEDENCE ) ).isEqualTo( 30L * Integer.MAX_VALUE + Ordered.HIGHEST_PRECEDENCE );
		assertThat( AcrossModuleRole.POSTPROCESSOR.asPriority( Ordered.LOWEST_PRECEDENCE ) ).isEqualTo( 30L * Integer.MAX_VALUE + Ordered.LOWEST_PRECEDENCE );
	}

	@Test
	void noPriorityOverlap() {
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( Ordered.HIGHEST_PRECEDENCE ) )
				.isLessThan( AcrossModuleRole.INFRASTRUCTURE.asPriority( 0 ) )
				.isLessThan( AcrossModuleRole.INFRASTRUCTURE.asPriority( Ordered.LOWEST_PRECEDENCE ) );
		assertThat( AcrossModuleRole.INFRASTRUCTURE.asPriority( Ordered.LOWEST_PRECEDENCE ) )
				.isLessThan( AcrossModuleRole.APPLICATION.asPriority( Ordered.HIGHEST_PRECEDENCE ) );
		assertThat( AcrossModuleRole.APPLICATION.asPriority( Ordered.LOWEST_PRECEDENCE ) )
				.isLessThan( AcrossModuleRole.POSTPROCESSOR.asPriority( Ordered.HIGHEST_PRECEDENCE ) );
	}
}
