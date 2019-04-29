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
package com.foreach.across.core.context.info;

import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.DynamicAcrossModuleFactory;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.module.AcrossModuleBootstrapConfiguration;
import com.foreach.across.core.context.module.AcrossModuleDescriptor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestConfigurableAcrossModuleInfo
{
	@Mock
	private AcrossContextInfo contextInfo;

	@Test
	void propertiesFromSingleBootstrapConfiguration( @Mock AcrossModuleBootstrapConfiguration configuration, @Mock AcrossModuleDescriptor descriptor ) {
		when( configuration.getModuleDescriptor() ).thenReturn( descriptor );
		when( descriptor.getModuleName() ).thenReturn( "MyModule" );
		when( descriptor.getResourcesKey() ).thenReturn( "myResources" );
		when( descriptor.isEnabled() ).thenReturn( true );

		ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( contextInfo, configuration, 5 );
		assertThat( moduleInfo.getIndex() ).isEqualTo( 5 );
		assertThat( moduleInfo.getContextInfo() ).isSameAs( contextInfo );
		assertThat( moduleInfo.getModuleBootstrapConfiguration() ).isSameAs( configuration );

		assertThat( moduleInfo.getName() ).isEqualTo( "MyModule" );
		assertThat( moduleInfo.getResourcesKey() ).isEqualTo( "myResources" );
		assertThat( moduleInfo.isEnabled() ).isTrue();
	}

	@Test
	void regularModuleNameAndAliases() {
		ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( null, new EmptyAcrossModule( "myModule" ), 10 );
		assertThat( "myModule" ).isEqualTo( moduleInfo.getName() );
		assertThat( moduleInfo.getAliases() ).isEmpty();
	}

	@SneakyThrows
	@Test
	void dynamicModuleNameAndAliases() {
		DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory();
		DynamicAcrossModule.DynamicApplicationModule module = (DynamicAcrossModule.DynamicApplicationModule) factory.setModuleName( "MyModule" ).getObject();

		ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( null, module, 10 );
		assertThat( "MyModule" ).isEqualTo( moduleInfo.getName() );
		assertThat( moduleInfo.getAliases() ).containsExactly( "DynamicApplicationModule" );
	}
}
