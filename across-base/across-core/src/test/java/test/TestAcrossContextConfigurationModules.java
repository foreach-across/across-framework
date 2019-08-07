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
package test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 4.0.0
 */
@DisplayName("Default modules added to each context")
class TestAcrossContextConfigurationModules
{
	private static AcrossContext context = new AcrossContext();
	private static AcrossContextInfo contextInfo;

	@BeforeAll
	static void bootstrap() {
		context.bootstrap();
		contextInfo = AcrossContextUtils.getContextInfo( context );
	}

	@AfterAll
	static void shutdown() {
		context.shutdown();
	}

	@Test
	@DisplayName("AcrossContextInfrastructureModule")
	void infrastructureModule() {
		AcrossModuleInfo moduleInfo = contextInfo.getModuleInfo( AcrossBootstrapConfigurer.CONTEXT_INFRASTRUCTURE_MODULE );
		assertThat( moduleInfo ).isNotNull();
		assertThat( moduleInfo.getModuleRole() ).isEqualTo( AcrossModuleRole.INFRASTRUCTURE );
		assertThat( moduleInfo.getOrderInModuleRole() ).isEqualTo( Ordered.HIGHEST_PRECEDENCE + 1000 );
	}

	@Test
	@DisplayName("AcrossContextPostProcessorModule")
	void postProcessorModule() {
		AcrossModuleInfo moduleInfo = contextInfo.getModuleInfo( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE );
		assertThat( moduleInfo ).isNotNull();
		assertThat( moduleInfo.getModuleRole() ).isEqualTo( AcrossModuleRole.POSTPROCESSOR );
		assertThat( moduleInfo.getOrderInModuleRole() ).isEqualTo( Ordered.LOWEST_PRECEDENCE - 1000 );
	}
}
