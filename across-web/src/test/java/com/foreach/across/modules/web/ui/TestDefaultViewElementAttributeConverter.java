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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
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
	private ObjectMapper objectMapper;

	@Before
	public void before() {
		objectMapper = new ObjectMapper();
		converter = new DefaultViewElementAttributeConverter( objectMapper );
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
		MyObject myObject = new MyObject( "myname for you", 34 );

		assertEquals( "{\"name\":\"myname for you\",\"age\":34}", converter.apply( myObject ) );
		assertEquals( "{\"name\":\"myname for you\",\"age\":34}", converter.apply( (Supplier) () -> myObject ) );
	}

	@Test
	public void customObjectToJsonWithCustomSerializer() {
		SimpleModule module = new SimpleModule();
		module.addSerializer( MyObject.class, new MyObjectSerializer() );
		objectMapper.registerModule( module );

		MyObject myObject = new MyObject( "myname for you", 34 );

		assertEquals( "{\"myobject\":\"custom\"}", converter.apply( myObject ) );
		assertEquals( "{\"myobject\":\"custom\"}", converter.apply( (Supplier) () -> myObject ) );
	}

	static class MyObject
	{
		private String name;
		private int age;

		public MyObject( String name, int age ) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}
	}

	private static class MyObjectSerializer extends JsonSerializer<MyObject>
	{
		@Override
		public Class<MyObject> handledType() {
			return MyObject.class;
		}

		@Override
		public void serialize( MyObject value,
		                       JsonGenerator gen,
		                       SerializerProvider serializers ) throws IOException, JsonProcessingException {
			gen.writeStartObject();
			gen.writeStringField( "myobject", "custom" );
			gen.writeEndObject();
		}
	}
}
