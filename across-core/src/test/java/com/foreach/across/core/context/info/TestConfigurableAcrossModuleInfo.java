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
package com.foreach.across.core.context.info;

import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.DynamicAcrossModuleFactory;
import com.foreach.across.core.EmptyAcrossModule;
import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestConfigurableAcrossModuleInfo
{
	@Test
	public void regularModuleNameAndAliases() {
		ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( null, new EmptyAcrossModule( "myModule" ), 10 );
		assertEquals( "myModule", moduleInfo.getName() );
		assertArrayEquals( new String[0], moduleInfo.getAliases() );
	}

	@SneakyThrows
	@Test
	public void dynamicModuleNameAndAliases() {
		DynamicAcrossModuleFactory factory = new DynamicAcrossModuleFactory();
		DynamicAcrossModule.DynamicApplicationModule module = (DynamicAcrossModule.DynamicApplicationModule) factory.setModuleName( "MyModule" ).getObject();

		ConfigurableAcrossModuleInfo moduleInfo = new ConfigurableAcrossModuleInfo( null, module, 10 );
		assertEquals( "MyModule", moduleInfo.getName() );
		assertArrayEquals( new String[] { "DynamicApplicationModule" }, moduleInfo.getAliases() );
	}
}
