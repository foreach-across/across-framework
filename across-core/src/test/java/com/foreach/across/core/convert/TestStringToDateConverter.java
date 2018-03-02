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
package com.foreach.across.core.convert;

import com.foreach.across.core.AcrossContext;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestStringToDateConverter
{
	private GenericApplicationContext parent;
	private AcrossContext context;
	private ConversionService conversionService = null;
	private StringToDateTimeConverter converter = null;

	private final TypeDescriptor STRING_DESCRIPTOR = TypeDescriptor.valueOf( String.class );
	private final TypeDescriptor DATE_DESCRIPTOR = TypeDescriptor.valueOf( Date.class );
	private final TypeDescriptor LOCALDATE_DESCRIPTOR = TypeDescriptor.valueOf( LocalDate.class );
	private final TypeDescriptor LOCALTIME_DESCRIPTOR = TypeDescriptor.valueOf( LocalTime.class );
	private final TypeDescriptor LOCALDATETIME_DESCRIPTOR = TypeDescriptor.valueOf( LocalDateTime.class );
	private final TypeDescriptor ZONEDDATETIME_DESCRIPTOR = TypeDescriptor.valueOf( ZonedDateTime.class );
	private final TypeDescriptor OFFSETDATETIME_DESCRIPTOR = TypeDescriptor.valueOf( OffsetDateTime.class );
	private final TypeDescriptor OFFSETTIME_DESCRIPTOR = TypeDescriptor.valueOf( OffsetTime.class );

	@Before
	public void setUp() {
		parent = new GenericApplicationContext();
		parent.refresh();

		context = new AcrossContext( parent );

		context.bootstrap();
		assertThat( context.isBootstrapped() ).isTrue();
		conversionService = parent.getBean( ConversionService.class );
		assertThat( conversionService ).isNotNull();
		converter = new StringToDateTimeConverter( conversionService );
	}

	@After
	public void shutdown() {
		context.shutdown();
		parent.close();
	}

	@Test
	public void blankStringReturnsNull() {
		assertThat( convert( " ", LOCALDATETIME_DESCRIPTOR ) ).isNull();
		assertThat( convert( "", DATE_DESCRIPTOR ) ).isNull();
	}

	@Test
	public void matches() {
		assertThat( converter.matches( STRING_DESCRIPTOR, DATE_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, LOCALDATE_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, LOCALTIME_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, LOCALDATETIME_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, ZONEDDATETIME_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, OFFSETDATETIME_DESCRIPTOR ) ).isTrue();
		assertThat( converter.matches( STRING_DESCRIPTOR, OFFSETTIME_DESCRIPTOR ) ).isTrue();
	}

	@Test
	public void dateLocalized() throws ParseException {
		converter.setLocale( Locale.GERMANY );
		Date date = DateUtils.parseDate( "2017-01-07 12:01", "yyyy-MM-dd HH:mm" );
		assertThat( convert( "Samstag, Jan 07, 2017 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Samstag, Jan 07, 2017 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		converter.setLocale( Locale.FRENCH );
		date = DateUtils.parseDate( "2017-05-07 12:01", "yyyy-MM-dd HH:mm" );
		assertThat( convert( "Dimanche, Mai 07, 2017 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Dimanche, Mai 07, 2017 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void localDateTimeLocalized() {
		converter.setLocale( Locale.GERMANY );
		LocalDateTime date = LocalDateTime.parse( "2017-01-07 12:01", DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) );
		assertThat( convert( "Samstag, Jan 07, 2017 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Samstag, Jan 07, 2017 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		converter.setLocale( Locale.FRANCE );
		date = LocalDateTime.parse( "2017-05-07 12:01", DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) );
		assertThat( convert( "Dimanche, Mai 07, 2017 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Dimanche, Mai 07, 2017 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsDate() throws ParseException {
		Date date = DateUtils.parseDate( "2017-05-23 00:00", "yyyy-MM-dd HH:mm" );
		// yyyy-MM-dd
		assertThat( convert( "2017-05-23", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy
		assertThat( convert( "23 May 2017", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy
		assertThat( convert( "May 23, 2017", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd
		assertThat( convert( "2017-May-23", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy
		assertThat( convert( "Tuesday, May 23, 2017", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyyMMdd
		assertThat( convert( "20170523", DATE_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsDateWithTime() throws ParseException {
		Date date = DateUtils.parseDate( "2017-01-07 12:01", "yyyy-MM-dd HH:mm" );
		// yyyy-MM-dd HH:mm
		assertThat( convert( "2017-01-07 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd HH:mm:ss
		assertThat( convert( "2017-01-07 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd HH:mm:ss z
		assertThat( convert( "2017-01-07 12:01:00 +0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07 12:01:00 CET", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd HH:mm:ss.SSS
		assertThat( convert( "2017-01-07 12:01:00.000", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd'T'HH:mm:ss.SSSZ
		assertThat( convert( "2017-01-07T12:01:00.000+0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd'T'HH:mm:ss.SSSXXX
		assertThat( convert( "2017-01-07T12:01:00.000+01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEE MMM dd HH:mm:ss z yyyy
		assertThat( convert( "Sat Jan 07 12:01:00 CET 2017", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm
		assertThat( convert( "07 Jan 2017 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm:ss
		assertThat( convert( "07 Jan 2017 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm:ss z
		assertThat( convert( "07 Jan 2017 12:01:00 +0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00 CET", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm:ss.SSS
		assertThat( convert( "07 Jan 2017 12:01:00.000", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm:ss.SSSZ
		assertThat( convert( "07 Jan 2017 12:01:00.000+0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// dd MMM yyyy HH:mm:ss.SSSXXX
		assertThat( convert( "07 Jan 2017 12:01:00.000+01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm
		assertThat( convert( "Jan 07, 2017 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm:ss
		assertThat( convert( "Jan 07, 2017 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm:ss z
		assertThat( convert( "Jan 07, 2017 12:01:00 +0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00 CET", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm:ss.SSS
		assertThat( convert( "Jan 07, 2017 12:01:00.000", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm:ss.SSSZ
		assertThat( convert( "Jan 07, 2017 12:01:00.000+0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// MMM dd, yyyy HH:mm:ss.SSSXXX
		assertThat( convert( "Jan 07, 2017 12:01:00.000+01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd HH:mm
		assertThat( convert( "2017-Jan-07 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd HH:mm:ss
		assertThat( convert( "2017-Jan-07 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd HH:mm:ss.SSS
		assertThat( convert( "2017-Jan-07 12:01:00.000", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd'T'HH:mm:ss.SSSZ
		assertThat( convert( "2017-Jan-07T12:01:00.000+0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MMM-dd'T'HH:mm:ss.SSSXXX
		assertThat( convert( "2017-Jan-07T12:01:00.000+01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm
		assertThat( convert( "Saturday, Jan 07, 2017 12:01", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm:ss
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm:ss z
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00 +0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00 CET", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm:ss.SSS
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm:ss.SSSZ
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000+0100", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// EEEE, MMM dd, yyyy HH:mm:ss.SSSXXX
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000+01:00", DATE_DESCRIPTOR ) ).isEqualTo( date );
		// yyyy-MM-dd'T'HH:mm:ss'Z'
		assertThat( convert( "2017-01-07T12:01:00Z", DATE_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsLocalDate() {
		LocalDate date = LocalDate.parse( "2017-05-23", DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
		assertThat( convert( "2017-05-23", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "23 May 2017", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "May 23, 2017", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-May-23", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Tuesday, May 23, 2017", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "20170523", LOCALDATE_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsLocalTime() {
		LocalTime time = LocalTime.parse( "10:32", DateTimeFormatter.ofPattern( "HH:mm" ) );
		assertThat( convert( "10:32", LOCALTIME_DESCRIPTOR ) ).isEqualTo( time );
		assertThat( convert( "10:32:00", LOCALTIME_DESCRIPTOR ) ).isEqualTo( time );
		assertThat( convert( "10:32:00.000", LOCALTIME_DESCRIPTOR ) ).isEqualTo( time );
	}

	@Test
	public void convertLocalTimeToLocalDateTime() {
		LocalTime time = LocalTime.parse( "10:32", DateTimeFormatter.ofPattern( "HH:mm" ) );
		LocalDateTime localDateTime = LocalDateTime.of( LocalDate.now(), time );
		assertThat( convert( "10:32", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( localDateTime );
		assertThat( convert( "10:32:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( localDateTime );
		assertThat( convert( "10:32:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( localDateTime );
	}

	@Test
	public void convertLocalTimeToLocalDate() {
		LocalDate localDate = LocalDate.now();
		assertThat( convert( "10:32", LOCALDATE_DESCRIPTOR ) ).isEqualTo( localDate );
		assertThat( convert( "10:32:00", LOCALDATE_DESCRIPTOR ) ).isEqualTo( localDate );
		assertThat( convert( "10:32:00.000", LOCALDATE_DESCRIPTOR ) ).isEqualTo( localDate );
	}

	@Test
	public void convertsLocalDateTime() {
		LocalDateTime date = LocalDateTime.parse( "2017-01-07 12:01", DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) );
		assertThat( convert( "2017-01-07 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07 12:01:00 +0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07 12:01:00 CET", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07 12:01:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07T12:01:00.000+0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07T12:01:00.000+0200", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date.plusHours( 1 ) );
		assertThat( convert( "2017-01-07T12:01:00.000+01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Sat Jan 07 12:01:00 CET 2017", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00 +0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00 CET", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00 Asia/Macao", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date.plusHours( 7 ) );
		assertThat( convert( "07 Jan 2017 12:01:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00.000+0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "07 Jan 2017 12:01:00.000+01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00 +0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00 CET", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00.000+0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Jan 07, 2017 12:01:00.000+01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-Jan-07 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-Jan-07 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-Jan-07 12:01:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-Jan-07T12:01:00.000+0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-Jan-07T12:01:00.000+01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00 +0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00 CET", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000+0100", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "Saturday, Jan 07, 2017 12:01:00.000+01:00", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-01-07T12:01:00Z", LOCALDATETIME_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsZonedDateTime() {
		ZoneId zoneId = ( (ZonedDateTime) convert( "2017-08-21 12:01:00 Europe/Paris", ZONEDDATETIME_DESCRIPTOR ) ).getZone();
		ZonedDateTime date = ZonedDateTime.of( LocalDateTime.parse( "2017-08-21T12:01" ), ZoneId.of( "Europe/Paris" ) );
		assertThat( convert( "2017-08-21 12:01:00 Europe/Paris", ZONEDDATETIME_DESCRIPTOR ) ).isEqualTo( date );

		date = ZonedDateTime.of( LocalDateTime.parse( "2017-01-07T12:01" ), ZoneId.of( "Europe/Paris" ) );
		assertThat( convert( "07 Jan 2017 12:01:00 Europe/Paris", ZONEDDATETIME_DESCRIPTOR ) ).isEqualTo( date );

		date = ZonedDateTime.of( LocalDateTime.parse( "2017-01-07T12:01" ), ZoneId.of( "Asia/Macao" ) );
		assertThat( convert( "07 Jan 2017 12:01:00 Asia/Macao", ZONEDDATETIME_DESCRIPTOR ) ).isEqualTo( date );
	}

	@Test
	public void convertsOffsetDateTime() {
		OffsetDateTime date = OffsetDateTime.of( LocalDateTime.parse( "2017-03-10 12:01",
		                                                              DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) ), ZoneOffset.ofHours( 1 ) );
		assertThat( convert( "2017-03-10 12:01:00 +0100", OFFSETDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-03-10 12:01:00 +01:00", OFFSETDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-03-10 12:01:00+01:00", OFFSETDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-03-10T12:01:00.000+0200", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.plusHours( 1 ).withOffsetSameLocal( ZoneOffset.ofHours( 2 ) ) );
		assertThat( convert( "10 Mar 2017 12:01:00 CET", OFFSETDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "10 Mar 2017 12:01:00 Asia/Macao", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.plusHours( 7 ).withOffsetSameLocal( ZoneOffset.ofHours( 8 ) ) );
	}

	@Test
	public void convertsOffsetTime() {
		LocalTime localTime = LocalTime.parse( "10:32", DateTimeFormatter.ofPattern( "HH:mm" ) );
		OffsetTime time = OffsetTime.of( localTime, ZoneOffset.ofHours( 1 ) );
		assertThat( convert( "10:32", OFFSETTIME_DESCRIPTOR ) ).isEqualTo( time );
		assertThat( convert( "10:32:00", OFFSETTIME_DESCRIPTOR ) ).isEqualTo( time );
		assertThat( convert( "10:32:00 +0200", OFFSETTIME_DESCRIPTOR ) )
				.isEqualTo( time.plusHours( 1 ).withOffsetSameLocal( ZoneOffset.ofHours( 2 ) ) );
		assertThat( convert( "10:32:00.000+03:00", OFFSETTIME_DESCRIPTOR ) )
				.isEqualTo( time.plusHours( 2 ).withOffsetSameLocal( ZoneOffset.ofHours( 3 ) ) );
	}

	@Test
	public void convertsOffsetDateTimeForSpecificZone() {
		OffsetDateTime date = OffsetDateTime.of( LocalDateTime.parse( "2017-03-10 12:01",
		                                                              DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) ), ZoneOffset.ofHours( 3 ) );
		converter.setZoneId( ZoneOffset.ofHours( 3 ) );
		assertThat( convert( "2017-03-10 12:01:00 +0300", OFFSETDATETIME_DESCRIPTOR ) ).isEqualTo( date );
		assertThat( convert( "2017-03-10 12:01:00 +0100", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.minusHours( 2 ).withOffsetSameLocal( ZoneOffset.ofHours( 1 ) ) );
		assertThat( convert( "2017-03-10T12:01:00.000+0200", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.minusHours( 1 ).withOffsetSameLocal( ZoneOffset.ofHours( 2 ) ) );
		assertThat( convert( "10 Mar 2017 12:01:00 CET", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.minusHours( 2 ).withOffsetSameLocal( ZoneOffset.ofHours( 1 ) ) );
		assertThat( convert( "10 Mar 2017 12:01:00 Asia/Macao", OFFSETDATETIME_DESCRIPTOR ) )
				.isEqualTo( date.plusHours( 5 ).withOffsetSameLocal( ZoneOffset.ofHours( 8 ) ) );
	}

	private Object convert( String source, TypeDescriptor requestedType ) {
		return converter.convert( source, STRING_DESCRIPTOR, requestedType );
	}

}
