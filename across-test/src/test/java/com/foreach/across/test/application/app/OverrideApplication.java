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
package com.foreach.across.test.application.app;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.test.application.app.modules.one.ModuleOne;
import com.foreach.across.test.application.app.modules.two.ModuleTwo;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Arne Vandamme
 * @since 3.2.1
 */
@AcrossApplication
public class OverrideApplication
{
	@Bean
	ModuleOne moduleOne() {
		return new ModuleOne();
	}

	@Bean
	ModuleTwo moduleTwo() {
		return new ModuleTwo();
	}

	public static void main( String[] args ) {
		new SpringApplication( OverrideApplication.class ).run( args );
	}
}
