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

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.*;

import static java.time.temporal.ChronoField.*;

/**
 * Default converter that tries a long list of default patterns for parsing a String back to a {@link Date} or a {@link TemporalAccessor}
 * for a fixed (default: US) locale. This converter is registered by default when the AcrossContext creates a
 * {@link org.springframework.core.convert.ConversionService}.
 * <p>
 * A blank string is considered to be a null date but will not result in a conversion exception.
 *
 * @author Arne Vandamme
 */
public class StringToDateConverter implements GenericConverter, ConditionalGenericConverter
{
	static final String[] DEFAULT_PATTERNS = {
			"yyyy-MM-dd",
			"yyyy-MM-dd HH:mm",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm:ss z",
			"yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
			"EEE MMM dd HH:mm:ss z yyyy",
			"dd MMM yyyy",
			"dd MMM yyyy HH:mm",
			"dd MMM yyyy HH:mm:ss",
			"dd MMM yyyy HH:mm:ss z",
			"dd MMM yyyy HH:mm:ss.SSS",
			"dd MMM yyyy HH:mm:ss.SSSZ",
			"dd MMM yyyy HH:mm:ss.SSSXXX",
			"MMM dd, yyyy",
			"MMM dd, yyyy HH:mm",
			"MMM dd, yyyy HH:mm:ss",
			"MMM dd, yyyy HH:mm:ss z",
			"MMM dd, yyyy HH:mm:ss.SSS",
			"MMM dd, yyyy HH:mm:ss.SSSZ",
			"MMM dd, yyyy HH:mm:ss.SSSXXX",
			"yyyy-MMM-dd",
			"yyyy-MMM-dd HH:mm",
			"yyyy-MMM-dd HH:mm:ss",
			"yyyy-MMM-dd HH:mm:ss.SSS",
			"yyyy-MMM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MMM-dd'T'HH:mm:ss.SSSXXX",
			"EEEE, MMM dd, yyyy",
			"EEEE, MMM dd, yyyy HH:mm",
			"EEEE, MMM dd, yyyy HH:mm:ss",
			"EEEE, MMM dd, yyyy HH:mm:ss z",
			"EEEE, MMM dd, yyyy HH:mm:ss.SSS",
			"EEEE, MMM dd, yyyy HH:mm:ss.SSSZ",
			"EEEE, MMM dd, yyyy HH:mm:ss.SSSXXX",
			"yyyyMMdd",
			"yyyy-MM-dd'T'HH:mm:ss'Z'"
	};

	static final String[] DEFAULT_PATTERNS_TEMPORAL_ACCESSORS = {
			"yyyy-[MMM-dd[['T'][ ]HH:mm[:ss[[ ]z][.SSS[Z][XXX]]]]][MM-dd[['T'][ ]HH:mm[:ss['Z'][[ ]z][ XX][.SSS[Z][XXX]]]]]",
			"EEE MMM dd HH:mm:ss z yyyy",
			"EEEE, MMM dd, yyyy[ HH:mm[:ss[[ ]z][ XX][.SSS[Z][XXX]]]]",
			"dd MMM yyyy[ HH:mm[:ss[[ ]z][ XX][.SSS[Z][XXX]]]]",
			"MMM dd, yyyy[ HH:mm[:ss[[ ]z][ XX][.SSS[Z][XXX]]]]",
			"yyyyMMdd",
			"HH:mm[:ss[[ ]z][ XX][.SSS[Z][XXX]]]"
	};

	static final Set<ConvertiblePair> CONVERTIBLE_TYPES = new HashSet<>(
			Arrays.asList( new ConvertiblePair( String.class, Date.class ),
			               new ConvertiblePair( String.class, LocalDate.class ),
			               new ConvertiblePair( String.class, LocalTime.class ),
			               new ConvertiblePair( String.class, LocalDateTime.class ),
			               new ConvertiblePair( String.class, ZonedDateTime.class ),
			               new ConvertiblePair( String.class, OffsetDateTime.class ),
			               new ConvertiblePair( String.class, OffsetTime.class ) )
	);

	private String[] patterns;
	private String[] temporalAccessorPatterns;
	private Locale locale;
	private ZoneId zoneId;
	private DateTimeFormatter dateTimeFormatter;
	private final ConversionService conversionService;

	public StringToDateConverter( ConversionService conversionService ) {
		this( conversionService, Locale.US, defaultPatterns(), defaultTemporalAccessorPatterns() );
	}

	public StringToDateConverter( ConversionService conversionService, Locale locale, String[] patterns ) {
		this( conversionService, locale, patterns, defaultTemporalAccessorPatterns() );
	}

	public StringToDateConverter( ConversionService conversionService, Locale locale, String[] patterns, String[] temporalAccessorPatterns ) {
		this( conversionService, locale, null, patterns, defaultTemporalAccessorPatterns() );
	}

	public StringToDateConverter( ConversionService conversionService, Locale locale, ZoneId zoneId, String[] patterns, String[] temporalAccessorPatterns ) {
		this.locale = locale;
		this.conversionService = conversionService;
		setPatterns( patterns );
		setTemporalAccessorPatterns( temporalAccessorPatterns );
		setZoneId( zoneId );
	}

	public void setLocale( Locale locale ) {
		this.locale = locale;
		dateTimeFormatter = dateTimeFormatter.withLocale( locale );
	}

	public void setPatterns( @NonNull String[] patterns ) {
		this.patterns = patterns.clone();
	}

	public void setZoneId( ZoneId zoneId ) {
		this.zoneId = zoneId != null ? zoneId : ZoneId.systemDefault();
		dateTimeFormatter = dateTimeFormatter.withZone( zoneId );
	}

	public void setTemporalAccessorPatterns( @NonNull String[] temporalAccessorPatterns ) {
		this.temporalAccessorPatterns = temporalAccessorPatterns.clone();
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseCaseInsensitive();
		Arrays.stream( this.temporalAccessorPatterns )
		      .forEach( pattern -> builder.appendOptional( DateTimeFormatter.ofPattern( pattern ) ) );
		dateTimeFormatter = builder.toFormatter().withLocale( locale );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object convert( Object data, TypeDescriptor sourceType, TypeDescriptor targetType ) {
		Object convertedValue = null;
		String source = (String) data;
		if ( StringUtils.isBlank( source ) ) {
			return convertedValue;
		}
		Class<?> type = targetType.getType();
		if ( Date.class.isAssignableFrom( type ) ) {
			try {
				convertedValue = DateUtils.parseDate( source, locale, patterns );
			}
			catch ( ParseException pe ) {
				throw new RuntimeException( pe );
			}
		}
		else if ( TemporalAccessor.class.isAssignableFrom( type ) ) {
			convertedValue = dateTimeFormatter.parse( source, new TemporalAccessorQuery() );
		}
		if ( !type.isInstance( convertedValue ) ) {
			convertedValue = conversionService.convert( convertedValue, type );
		}
		return convertedValue;
	}

	/**
	 * @return the set of default patterns used to convert {@link Date}s.
	 */
	public static String[] defaultPatterns() {
		return DEFAULT_PATTERNS.clone();
	}

	/**
	 * @return the set of default patterns used to convert {@link TemporalAccessor}s.
	 */
	public static String[] defaultTemporalAccessorPatterns() {
		return DEFAULT_PATTERNS_TEMPORAL_ACCESSORS.clone();
	}

	@Override
	public boolean matches( TypeDescriptor sourceType, TypeDescriptor targetType ) {
		// AX-176 Don't match @RequestParam when @DateTimeFormat is specified
		return isDateType( targetType ) && targetType.getAnnotation( DateTimeFormat.class ) == null;
	}

	/**
	 * Check if the given type is one of the supported date types.
	 *
	 * @param targetType type to check
	 * @return whether the type is supported
	 */
	private boolean isDateType( TypeDescriptor targetType ) {
		return TemporalAccessor.class.isAssignableFrom( targetType.getType() ) || Date.class.isAssignableFrom( targetType.getType() );
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.unmodifiableSet( CONVERTIBLE_TYPES );
	}

	/**
	 * Converts the given {@link TemporalAccessor} to an {@link OffsetDateTime}
	 *
	 * @see java.time.temporal.TemporalQueries
	 */
	class TemporalAccessorQuery implements TemporalQuery<OffsetDateTime>
	{
		@Override
		public OffsetDateTime queryFrom( TemporalAccessor temporal ) {
			return OffsetDateTime.ofInstant( getZonedDateTime( temporal ).toInstant(), getZoneOffset( temporal ) );
		}

		/**
		 * Returns the {@link ZoneId} of the given temporal.
		 * If the temporal supports {@link java.time.temporal.ChronoField#OFFSET_SECONDS}, those will be used to define the zone.
		 * Otherwise the temporal will be queried for the zone. Should no zone be provided, {@link ZoneId#systemDefault()} will be used.
		 *
		 * @param temporal to retrieve the ZoneId of
		 * @return the ZoneId.
		 */
		private ZoneId getZoneOffset( TemporalAccessor temporal ) {
			if ( temporal.isSupported( OFFSET_SECONDS ) ) {
				return ZoneOffset.ofTotalSeconds( Math.toIntExact( temporal.getLong( OFFSET_SECONDS ) ) );
			}
			ZoneId zone = temporal.query( TemporalQueries.zoneId() );
			return ZoneOffset.of( zone != null ? zone.getId() : zoneId.getId(), ZoneId.SHORT_IDS );
		}

		/**
		 * Retrieves the LocalDateTime for the default zone.
		 *
		 * @param temporal to retrieve the date-time from.
		 * @return the date-time as a {@link ZonedDateTime}
		 */
		private ZonedDateTime getZonedDateTime( TemporalAccessor temporal ) {
			return LocalDateTime.of( getLocalDate( temporal ), getLocalTime( temporal ) ).atZone( zoneId );
		}

		private LocalDate getLocalDate( TemporalAccessor temporal ) {
			return temporal.isSupported( EPOCH_DAY ) ? LocalDate.ofEpochDay( temporal.getLong( EPOCH_DAY ) ) : LocalDate.now();
		}

		private LocalTime getLocalTime( TemporalAccessor temporal ) {
			return temporal.isSupported( NANO_OF_DAY ) ? LocalTime.ofNanoOfDay( temporal.getLong( NANO_OF_DAY ) ) : LocalTime.MIN;
		}
	}
}
