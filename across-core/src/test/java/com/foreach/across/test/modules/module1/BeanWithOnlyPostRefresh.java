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

package com.foreach.across.test.modules.module1;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.test.modules.module2.ScannedBeanModule2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Exposed
public class BeanWithOnlyPostRefresh
{
	private boolean refreshed = false;
	private boolean unknownBeanSet = false;
	private ScannedBeanModule2 scannedBeanFromLaterModule;

	public ScannedBeanModule2 getScannedBeanFromLaterModule() {
		return scannedBeanFromLaterModule;
	}

	public boolean isRefreshed() {
		return refreshed;
	}

	public boolean isUnknownBeanSet() {
		return unknownBeanSet;
	}

	@PostRefresh
	public void refresh() {
		refreshed = true;
	}

	@Autowired(required = false)
	@PostRefresh(required = false)
	private void setScannedBeanFromLaterModule( ScannedBeanModule2 scannedBeanFromLaterModule ) {
		this.scannedBeanFromLaterModule = scannedBeanFromLaterModule;
	}

	@PostRefresh(required = false)
	void setReallyUnknownBean( @Qualifier("someUnknownBean") ScannedBeanModule1 beanModule1 ) {
		this.unknownBeanSet = true;
	}
}
