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
package com.foreach.across.core.message.parser;

import com.foreach.across.core.message.MessageResolver;
import com.foreach.across.core.message.ResolvableMessage;
import com.foreach.across.core.message.ResolvableMessageFormat;
import com.foreach.across.core.message.ResolvableMessageFormatContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static com.foreach.across.core.message.ResolvableMessageFormatContext.builder;
import static com.foreach.across.core.message.parser.ValueResolver.forIndexedArgument;
import static com.foreach.across.core.message.parser.ValueResolver.forNamedArgument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestResolvableMessageFormat
{
	@Mock
	private MessageResolver messageResolver;

	@Test
	public void simpleMessage() {
		assertThat( format( new Literal( "my message" ) ).format( context() ) ).isEqualTo( "my message" );
		assertThat( format( new Literal( "my message " ), new Literal( "for you" ) )
				            .format( context() ) ).isEqualTo( "my message for you" );
	}

	@Test
	public void indexedArgument() {
		assertThat(
				format( new FormattedArgument( forIndexedArgument( 0 ), null, null ) )
						.format( builder().locale( Locale.ENGLISH ).arguments( new Object[] { "my argument" } ).argumentNames( new String[1] ).build() )
		).isEqualTo( "my argument" );

		Date date = Date.from( LocalDate.of( 2018, 2, 17 ).atStartOfDay( ZoneId.systemDefault() ).toInstant() );
		assertThat(
				format( new FormattedArgument( forIndexedArgument( 0 ), "date", "dd MMM yyyy" ) )
						.format( builder().locale( Locale.ENGLISH ).arguments( new Object[] { date } ).argumentNames( new String[1] ).build() )
		).isEqualTo( "17 Feb 2018" );
	}

	@Test
	public void namedArgument() {
		assertThat(
				format( new FormattedArgument( forNamedArgument( "title" ), null, null ) ).format(
						builder().locale( Locale.ENGLISH )
						         .arguments( new Object[] { "my argument" } )
						         .argumentNames( new String[] { "title" } )
						         .build()
				)
		).isEqualTo( "my argument" );

		Date date = Date.from( LocalDate.of( 2018, 2, 17 ).atStartOfDay( ZoneId.systemDefault() ).toInstant() );
		assertThat(
				format( new FormattedArgument( forNamedArgument( "today" ), "date", "dd MMM yyyy" ) ).format(
						builder().locale( Locale.ENGLISH )
						         .arguments( new Object[] { date } )
						         .argumentNames( new String[] { "today" } )
						         .build() )
		).isEqualTo( "17 Feb 2018" );
	}

	@Test
	public void nestedMessage() {
		when( messageResolver.resolveMessage( ResolvableMessage.messageCode( "my.code" ), Locale.ENGLISH ) ).thenReturn( "my argument" );

		assertThat(
				format( new MessageLookup( new String[] { "my.code" } ) ).format(
						builder().locale( Locale.ENGLISH )
						         .messageResolver( messageResolver )
						         .build()
				)
		).isEqualTo( "my argument" );
	}

	private ResolvableMessageFormatContext context() {
		return ResolvableMessageFormatContext.builder().locale( Locale.FRANCE ).build();
	}

	private ResolvableMessageFormat format( MessageToken... tokens ) {
		return new ResolvableMessageFormat( Arrays.asList( tokens ) );
	}
}
