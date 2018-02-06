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
package com.foreach.across.core.message;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static com.foreach.across.core.message.ResolvableMessage.messageCode;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestMessageSourceMessageResolver
{
	private ResourceBundleMessageSource messageSource;
	private MessageResolver messageResolver;

	@Before
	public void before() {
		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename( "messages/parameterized" );

		messageResolver = new MessageSourceMessageResolver( messageSource );
	}

	@Test
	public void messageSourceIsLoadedCorrectly() {
		assertThat( messageSource.getMessage( "parameters.none", new Object[0], null ) ).isEqualTo( "message without parameters" );
	}

	@Test
	public void messageSourceWithoutArgumentsDoesNotStripThem() {
		assertThat( messageSource.getMessage( "parameters.indexed", new Object[0], null ) ).isEqualTo( "{0} and {1}" );
		assertThat( messageSource.getMessage( "parameters.named", new Object[0], null ) ).isEqualTo( "{value} and {otherValue}" );

		MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable( new String[] { "parameters.named", "parameters.indexed" }, new Object[0] );
		assertThat( messageSource.getMessage( resolvable, null ) ).isEqualTo( "{value} and {otherValue}" );

		resolvable = new DefaultMessageSourceResolvable( new String[] { "bad.parameters.named", "bad.parameters.indexed" }, new Object[0], "{0} and {named}" );
		assertThat( messageSource.getMessage( resolvable, null ) ).isEqualTo( "{0} and {named}" );
	}

	@Test
	public void messageSourceDefaultValueFormatting() {
		Date date = Date.from( LocalDate.of( 2018, 2, 17 ).atStartOfDay( ZoneId.systemDefault() ).toInstant() );
		MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable( new String[] { "invalid-code" }, new Object[] { date },
		                                                                         "{0,date,dd MMM yyyy}" );
		assertThat( messageSource.getMessage( resolvable, Locale.UK ) ).isEqualTo( "17 Feb 2018" );

		assertThat( messageResolver.resolveMessage( messageCode( "invalid-code" ).withParameter( "date", date ).withDefaultValue( "{0,date,dd MMM yyyy}" ) ) )
				.isEqualTo( "17 Feb 2018" );
	}

	@Test
	public void noParameters() {
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.none" ) ) ).isEqualTo( "message without parameters" );
	}

	@Test
	public void indexParameters() {
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.indexed" )
				                                            .withParameter( "otherValue", "my other value" )
				                                            .withParameter( "value", "my value" ) ) )
				.isEqualTo( "my other value and my value" );
	}

	@Test
	public void namedParameters() {
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.named" )
				                                            .withParameter( "otherValue", "my other value" )
				                                            .withParameter( "value", "my value" ) ) )
				.isEqualTo( "my value and my other value" );
	}

	@Test
	public void dateFormat() {
		Date date = Date.from( LocalDate.of( 2018, 2, 17 ).atStartOfDay( ZoneId.systemDefault() ).toInstant() );
		assertThat( messageSource.getMessage( "parameters.formatted.simpledate", new Object[] { date }, Locale.UK ) ).isEqualTo( "17 Feb 2018" );

		assertThat( messageResolver.resolveMessage( messageCode( "parameters.formatted.simpledate" ).withParameter( "date", date ) ) )
				.isEqualTo( "17 Feb 2018" );
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.formatted.dates" ).withParameter( "date", date ) ) )
				.isEqualTo( "17 Feb 2018 2018 Feb 17" );
	}

	@Test
	public void nestedMessage() {
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.nested" ) ) ).isEqualTo( "nested message without parameters" );
	}

	@Test
	public void nestedMessageWithSameParameters() {
		assertThat( messageResolver.resolveMessage( messageCode( "parameters.nestedWithSameParameters" )
				                                            .withParameter( "otherValue", "my other value" )
				                                            .withParameter( "value", "my value" ) ) )
				.isEqualTo( "nested my other value and my value, my value and my other value" );
	}

	// use converter by default if no type specified
	// use date format for java.time objects
	// use message format for the default types

}
