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
package test.validation;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestMethodValidation.Config.class)
public class TestMethodValidation
{
	@Autowired
	private MyService myService;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void singleValidatorShouldBePresent() {
		assertEquals( 1, beanRegistry.getBeansOfType( Validator.class, true ).size() );
	}

	@Test
	public void methodReturnsIfValidatedOk() {
		assertTrue( myService.validateArguments( 56, "my value" ) );
	}

	@Test
	public void methodFailsIfNumberNotInRange() {
		assertThatExceptionOfType( ConstraintViolationException.class )
				.isThrownBy( () -> myService.validateArguments( 1, "my value" ) )
				.satisfies( exception -> hasConstraintMessage( exception, "must be greater than or equal to 10" ) );
	}

	@Test
	public void methodFailsIfValueNull() {
		assertThatExceptionOfType( ConstraintViolationException.class )
				.isThrownBy( () -> myService.validateArguments( 56, null ) )
				.satisfies( exception -> hasConstraintMessage( exception, "must not be null" ) );
	}

	private void hasConstraintMessage( ConstraintViolationException exception, String message ) {
		val violations = exception.getConstraintViolations();
		assertEquals( 1, violations.size() );
		ConstraintViolation violation = violations.iterator().next();
		assertEquals( message, violation.getMessage() );
	}

	@Configuration
	@EnableAcrossContext
	static class Config
	{
		@Bean
		public AcrossModule validatingModule() {
			return new EmptyAcrossModule( "validatingModule", MyService.class );
		}
	}

	@Service
	@Validated
	static class MyService
	{
		@SuppressWarnings("all")
		boolean validateArguments( @Min(10) @Max(100) int number, @NotNull String value ) {
			return true;
		}
	}
}
