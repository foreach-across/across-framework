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
package test.modules.settings;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Date;

/**
 * @author Arne Vandamme
 */
@ConfigurationProperties("settings")
public class SettingsModuleSettings extends AcrossModuleSettings
{
	private int index;
	private Date date;

	public int getIndex() {
		return index;
	}

	public void setIndex( int index ) {
		this.index = index;
	}

	public Date getDate() {
		return date;
	}

	public void setDate( Date date ) {
		this.date = date;
	}

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
	}

	public boolean isActive() {
		return getRequiredProperty( "settings.active", Boolean.class );
	}
}
