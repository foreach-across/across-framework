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
package com.foreach.across.modules.web.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Date;
import java.util.function.Supplier;

/**
 * Default implementation of {@link ViewElementAttributeConverter}.  If a raw value is a primitive or wrapper,
 * it will be converted to string using its {@link Object#toString()} method. Otherwise it will be converted
 * to a JSON string using the {@link ObjectMapper}.
 * <p/>
 * If the raw value is a {@link Supplier}, the value will be fetched from the supplier first, and then converted.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Component
public class DefaultViewElementAttributeConverter implements ViewElementAttributeConverter
{
	private final static FastDateFormat DATE_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd HH:mm:ss" );

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String apply( Object baseValue ) {
		Object value = baseValue instanceof Supplier ? ( (Supplier) baseValue ).get() : baseValue;

		if ( value != null ) {
			if ( value instanceof String ) {
				return (String) value;
			}
			if ( ClassUtils.isPrimitiveOrWrapper( value.getClass() ) || value instanceof Number ) {
				return value.toString();
			}
			if ( value instanceof Date ) {
				return DATE_FORMAT.format( (Date) value );
			}

			try {
				return objectMapper.writeValueAsString( value );
			}
			catch ( JsonProcessingException jpe ) {
				throw new IllegalArgumentException( "Unable to convert value to JSON", jpe );
			}
		}

		return null;
	}
}
