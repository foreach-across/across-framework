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
package support;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.support.AcrossTestBuilders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class TestContexts
{
	public static final String MODULE_ONE = "one";
	public static final String MODULE_TWO = "two";

	/**
	 * Creates a test context with 2 modules, two internal (internalBean) and two exposed beans: numberOne and numberTwo.
	 */
	public static AcrossTestContext simple() {
		return AcrossTestBuilders.standard( false )
		                         .configurer( ctx -> ctx.setInstallerAction( InstallerAction.DISABLED ) )
		                         .modules(
				                         new EmptyAcrossModule( MODULE_ONE, ModuleOneConfiguration.class ),
				                         new EmptyAcrossModule( MODULE_TWO, ModuleTwoConfiguration.class )
		                         )
		                         .build();
	}

	/**
	 * Creates a test context with 2 modules and two exposed beans: numberOne and numberTwo
	 * The order of the modules is reversed in this configuration compared to {@link #simple()}.
	 */
	public static AcrossTestContext simpleReversed() {
		return AcrossTestBuilders.standard( false )
		                         .configurer( ctx -> ctx.setInstallerAction( InstallerAction.DISABLED ) )
		                         .modules(
				                         new EmptyAcrossModule( MODULE_TWO, ModuleTwoConfiguration.class ),
				                         new EmptyAcrossModule( MODULE_ONE, ModuleOneConfiguration.class )
		                         )
		                         .build();
	}

	@Configuration
	static class ModuleOneConfiguration
	{
		@Bean
		String internalBean() {
			return "internalOne";
		}

		@Bean
		@Exposed
		Integer numberOne() {
			return 1;
		}
	}

	@Configuration
	static class ModuleTwoConfiguration
	{
		@Bean
		String internalBean() {
			return "internalTwo";
		}

		@Bean
		@Exposed
		Integer numberTwo() {
			return 2;
		}
	}
}
