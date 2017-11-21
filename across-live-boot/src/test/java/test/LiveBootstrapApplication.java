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
package test;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.modules.web.AcrossWebModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Sample application configuration for Spring Boot execution.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@AcrossApplication(modules = AcrossWebModule.NAME)
@Import(ErrorMvcAutoConfiguration.class)
public class LiveBootstrapApplication
{
	@Bean
	public EmptyAcrossModule emptyModule() {
		return new EmptyAcrossModule( "emptyModule" );
	}

	public static void main( String[] args ) {
		SpringApplication.run( LiveBootstrapApplication.class );
	}
}
