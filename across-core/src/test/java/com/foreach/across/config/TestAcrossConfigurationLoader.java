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
package com.foreach.across.config;

import org.junit.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAcrossConfigurationLoader
{
	@Test
	public void allConfigurationFilesAreLoaded() {
		List<String> values = AcrossConfigurationLoader.loadValues( "com.foreach.across.AutoConfigurationEnabled",
		                                                            Thread.currentThread().getContextClassLoader() );

		assertTrue( values.contains( DataSourceAutoConfiguration.class.getName() ) );
		assertTrue( values.contains( "dummyClass" ) );
		assertTrue( values.contains( "anotherDummyClass:enablerClass" ) );

		Map<String, String> valueMap = AcrossConfigurationLoader.loadMapValues( "com.foreach.across.AutoConfigurationEnabled",
		                                                                        Thread.currentThread().getContextClassLoader() );
		assertEquals( DataSourceAutoConfiguration.class.getName(), valueMap.get( DataSourceAutoConfiguration.class.getName() ) );
		assertEquals( "dummyClass", valueMap.get( "dummyClass" ) );
		assertEquals( "enablerClass", valueMap.get( "anotherDummyClass" ) );
	}
}
