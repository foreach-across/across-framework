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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("AcrossModuleBootstrapConfigurationSet")
class TestAcrossModuleBootstrapConfigurationSet
{
	private AcrossModuleDescriptor descriptorOne = AcrossModuleDescriptor.builder()
	                                                                     .moduleName( "moduleOne" )
	                                                                     .moduleNameAlias( "alias1" )
	                                                                     .moduleNameAlias( "alias2" )
	                                                                     .resourcesKey( "defaultModule" )
	                                                                     .build();
	private AcrossModuleBootstrapConfiguration configurationOne = AcrossModuleBootstrapConfiguration.from( descriptorOne );

	private AcrossModuleDescriptor descriptorTwo = AcrossModuleDescriptor.builder()
	                                                                     .moduleName( "moduleTwo" )
	                                                                     .resourcesKey( "defaultModule" )
	                                                                     .requiredModule( "moduleOne" )
	                                                                     .build();
	private AcrossModuleBootstrapConfiguration configurationTwo = AcrossModuleBootstrapConfiguration.from( descriptorTwo );

	@Test
	@DisplayName("same configuration is returned for all aliases")
	void configurationByName() {
		AcrossModuleBootstrapConfigurationSet configurationSet = AcrossModuleBootstrapConfigurationSet.create( Collections.singleton( descriptorOne ) );
		assertThat( configurationSet ).isNotNull();
		assertThat( configurationSet.getConfigurationsInOrder() ).containsExactly( configurationOne );

		assertThat( configurationSet.getConfigurationForModule( "any" ) ).isEmpty();
		assertThat( configurationSet.getConfigurationForModule( "moduleOne" ) ).contains( configurationOne );
		assertThat( configurationSet.getConfigurationForModule( "alias1" ) ).contains( configurationOne );
		assertThat( configurationSet.getConfigurationForModule( "alias2" ) ).contains( configurationOne );
	}

	@Test
	@DisplayName("set without extensions")
	void withoutExtensions() {
		AcrossModuleBootstrapConfigurationSet configurationSet = AcrossModuleBootstrapConfigurationSet.create( Arrays.asList( descriptorTwo, descriptorOne ) );

		assertThat( configurationSet ).isNotNull();
		assertThat( configurationSet.getConfigurationsInOrder() ).containsExactly( configurationOne, configurationTwo );
	}

	@Test
	@DisplayName("set with extensions")
	void withExtensions() {
		AcrossModuleDescriptor descriptorThree = AcrossModuleDescriptor.builder()
		                                                               .moduleName( "moduleThree" )
		                                                               .resourcesKey( "defaultModule" )
		                                                               .extensionTargets( Arrays.asList( "unknownModule", "moduleOne" ) )
		                                                               .build();
		AcrossModuleBootstrapConfiguration configurationThree = AcrossModuleBootstrapConfiguration.from( descriptorThree );

		AcrossModuleBootstrapConfigurationSet configurationSet
				= AcrossModuleBootstrapConfigurationSet.create( Arrays.asList( descriptorThree, descriptorTwo, descriptorOne ) );

		assertThat( configurationSet ).isNotNull();
		assertThat( configurationSet.getConfigurationForModule( "moduleThree" ) ).contains( configurationThree );
		assertThat( configurationSet.getConfigurationForModule( "moduleOne" ) )
				.isPresent()
				.hasValueSatisfying( cfg -> assertThat( cfg.getExtensions() ).containsExactly( configurationThree ) );

		configurationOne.addExtension( configurationThree );

		assertThat( configurationSet.getConfigurationsInOrder() ).containsExactly( configurationOne, configurationTwo );
	}
}
