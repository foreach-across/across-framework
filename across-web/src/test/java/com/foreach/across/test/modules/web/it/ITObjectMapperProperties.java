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
package com.foreach.across.test.modules.web.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ITAcrossWebModule.Config.class)
@TestPropertySource(properties = "spring.jackson.date-format=yyyy-MM-dd-HH-mm")
public class ITObjectMapperProperties extends AbstractWebIntegrationTest
{
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@SneakyThrows
	public void useCustomDateSerialization() {
		SimpleDateFormat df = new SimpleDateFormat( "dd-MM-yyyy HH:mm" );
		df.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

		Date date = df.parse( "11-04-2017 13:45" );
		assertEquals( "\"2017-04-11-13-45\"", objectMapper.writeValueAsString( date ) );
	}

	@Test
	@SneakyThrows
	public void useCustomLocalDateTimeSerialization() {
		LocalDateTime ldt = LocalDateTime.of( 2017, Month.APRIL, 11, 13, 45 );

		assertEquals( "\"2017-04-11T13:45:00\"", objectMapper.writeValueAsString( ldt ) );
	}

	@EnableAcrossContext(modules = AcrossWebModule.NAME)
	@Configuration
	static class Config
	{
	}
}
