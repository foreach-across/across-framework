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
package com.foreach.across.test.modules.settings.config;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.test.modules.settings.SettingsModuleSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @author Arne Vandamme
 */
@Configuration
@Exposed
@AcrossCondition("settings.active")
public class SettingsConfig
{
	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private SettingsModuleSettings settings;

	public boolean isActive() {
		return settings.isActive();
	}

	public int getIndex() {
		return settings.getIndex();
	}

	public Date getDate() {
		return settings.getDate();
	}

	@Bean
	@ConditionalOnProperty("settings.active")
	public String someBean() {
		return "someBean";
	}
}
