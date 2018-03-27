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
package com.foreach.across.test.application;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@SuppressWarnings("WeakerAccess")
public class TestIllegalConfigurations
{
	@Test
	public void usingIllegalConfigurationOnApplication() {
		assertThatExceptionOfType( AcrossConfigurationException.class )
				.isThrownBy( () -> bootstrap( IllegalConfigurationOnApplication.class, true ) )
				.withMessageContaining( WebMvcConfigurationSupport.class.getName() );
	}

	@Test
	public void usingIllegalConfigurationOnModule() {
		assertThatExceptionOfType( BeanCreationException.class )
				.isThrownBy( () -> bootstrap( IllegalConfigurationInModule.class, true ) )
				.satisfies( bce -> assertThat( bce.getRootCause().getMessage() ).contains( WebMvcConfigurationSupport.class.getName() ) );
	}

	@Test
	public void disableValidation() {
		assertThatExceptionOfType( BeanCreationException.class )
				.isThrownBy( () -> bootstrap( IllegalConfigurationOnApplication.class, false ) )
				.satisfies( bce -> assertThat( bce.getRootCause() instanceof IllegalStateException ) );
	}

	private void bootstrap( Class<?> applicationClass, boolean validate ) {
		AcrossApplicationContext context = new AcrossApplicationContext();
		if ( !validate ) {
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment( context.getEnvironment(), "across.configuration.validate=false" );
		}
		context.register( applicationClass );
		context.refresh();
		context.start();
	}

	@AcrossApplication
	@EnableWebMvc
	protected static class IllegalConfigurationOnApplication
	{
	}

	@AcrossApplication
	protected static class IllegalConfigurationInModule
	{
		@Bean
		public EmptyAcrossModule myModule() {
			return new EmptyAcrossModule( "myModule", DelegatingWebMvcConfiguration.class );
		}
	}
}
