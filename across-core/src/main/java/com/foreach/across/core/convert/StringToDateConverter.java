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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Default converter that tries a long list of default patterns for parsing a String back to a Date
 * for a fixed (default: US) locale. This converter is registered by default when the AcrossContext creates a
 * {@link org.springframework.core.convert.ConversionService}.
 * <p/>
 * A blank string is considered to be a null date but will not result in a conversion exception.
 *
 * @author Arne Vandamme
 */
public class StringToDateConverter implements Converter<String, Date>
{
	public static final String[] DEFAULT_PATTERNS = {
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
			"EEEE, MMM dd, yyyy HH:mm:ss.SSSXXX"
	};

	private String[] patterns;
	private Locale locale;

	public StringToDateConverter() {
		this( Locale.US, DEFAULT_PATTERNS );
	}

	public StringToDateConverter( Locale locale, String[] patterns ) {
		this.locale = locale;
		this.patterns = patterns;
	}

	public void setLocale( Locale locale ) {
		this.locale = locale;
	}

	public void setPatterns( String[] patterns ) {
		this.patterns = patterns;
	}

	@Override
	public Date convert( String source ) {
		if ( StringUtils.isBlank( source ) ) {
			return null;
		}

		try {
			return DateUtils.parseDate( source, locale, patterns );
		}
		catch ( ParseException pe ) {
			throw new RuntimeException( pe );
		}
	}
}
