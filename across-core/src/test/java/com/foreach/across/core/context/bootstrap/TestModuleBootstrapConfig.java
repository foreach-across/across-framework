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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
class TestModuleBootstrapConfig
{
	private ModuleBootstrapConfig config = new ModuleBootstrapConfig( new ConfigurableAcrossModuleInfo( null, new EmptyAcrossModule( "myModule" ), 1 ) );

	@Test
	void configIsEmptyIfNoInstallerOrApplicationContextConfigurers() {
		assertTrue( config.isEmpty() );
	}

	@Test
	void configNonEmptyIfContextConfigurers() {
		config.addApplicationContextConfigurer( TestModuleBootstrapConfig.class );
		assertFalse( config.isEmpty() );
	}

	@Test
	void configNonEmptyIfClassesToImport() {
		config.extendModule( true, TestModuleBootstrapConfig.class.getName() );
		assertFalse( config.isEmpty() );
	}

	@Test
	void configStaysEmptyIfOptionalAdded() {
		config.addApplicationContextConfigurer( true, TestModuleBootstrapConfig.class );
		assertTrue( config.isEmpty() );
	}

	@Test
	void configNonEmptyIfThereAreInstallers() {
		config.setInstallers( Collections.singleton( "" ) );
		assertFalse( config.isEmpty() );
	}
}
