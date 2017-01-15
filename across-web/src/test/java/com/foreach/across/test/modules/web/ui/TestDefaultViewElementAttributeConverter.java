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
package com.foreach.across.test.modules.web.ui;

import com.foreach.across.modules.web.ui.DefaultViewElementAttributeConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestDefaultViewElementAttributeConverter
{
	private DefaultViewElementAttributeConverter converter;

	@Before
	public void before() {
		converter = new DefaultViewElementAttributeConverter();
	}

	@Test
	public void nullValueIsReturned() {
		assertNull( converter.apply( null ) );
		assertNull( converter.apply( (Supplier) () -> null ) );
	}

	@Test
	public void stringValue() {
		assertEquals( "123", converter.apply( "123" ) );
		assertEquals( "123", converter.apply( (Supplier) () -> "123" ) );
	}

	@Test
	public void primitivesOrWrappersUseToString() {
		assertEquals( "123", converter.apply( 123 ) );
		assertEquals( "123", converter.apply( (Supplier) () -> 123 ) );
		assertEquals( "656", converter.apply( 656L ) );
		assertEquals( "656", converter.apply( (Supplier) () -> 656L ) );
		assertEquals( "a", converter.apply( 'a' ) );
		assertEquals( "a", converter.apply( (Supplier) () -> 'a' ) );
	}

	@Test
	public void numbers() {
		assertEquals( "12.33", converter.apply( new BigDecimal( "12.33" ) ) );
		assertEquals( "12.33", converter.apply( (Supplier) () -> new BigDecimal( "12.33" ) ) );
	}

	@Test
	public void dateUsesUniversalPattern() throws ParseException {
		Date date = DateUtils.parseDate( "2016-01-01 13:45:55", "yyyy-MM-dd HH:mm:ss" );

		assertEquals( "2016-01-01 13:45:55", converter.apply( date ) );
		assertEquals( "2016-01-01 13:45:55", converter.apply( (Supplier) () -> date ) );
	}

	@Test
	public void customObjectToJson() {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put( "name", "myname for you" );
		json.put( "age", 34 );

		assertEquals( "{\"name\":\"myname for you\",\"age\":34}", converter.apply( json ) );
		assertEquals( "{\"name\":\"myname for you\",\"age\":34}", converter.apply( (Supplier) () -> json ) );
	}
}
