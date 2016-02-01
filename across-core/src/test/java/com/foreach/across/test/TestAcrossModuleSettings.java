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
package com.foreach.across.test;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.test.modules.settings.SettingsModule;
import com.foreach.across.test.modules.settings.config.SettingsConfig;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestAcrossModuleSettings.Config.class)
@TestPropertySource(properties = { "settings.active=true", "settings.index=2", "settings.date=2014-11-07 11:35" })
public class TestAcrossModuleSettings
{
	@Autowired
	private SettingsConfig config;

	@Test
	public void settingsConfiguration() {
		assertTrue( config.isActive() );
		assertEquals( 2, config.getIndex() );
	}

	@Test
	public void acrossConditionEvaluated() {
		// for compatibility, @AcrossCondition supports "settings" directly
		assertEquals( "someBean", config.someBean() );
	}

	@Test
	public void dateIsSet() {
		assertNotNull( config.getDate() );
		assertEquals( "2014-11-07 11:35", FastDateFormat.getInstance( "yyyy-MM-dd HH:mm" ).format( config.getDate() ) );
	}

	@EnableAcrossContext
	protected static class Config
	{
		@Bean
		public SettingsModule settingsModule() {
			return new SettingsModule();
		}
	}
}
