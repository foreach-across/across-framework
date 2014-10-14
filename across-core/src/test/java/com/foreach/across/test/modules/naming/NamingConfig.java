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

package com.foreach.across.test.modules.naming;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NamingConfig
{
	@Autowired
	private AcrossContext autoAcrossContext;

	@Autowired
	@Qualifier(AcrossContext.BEAN)
	private AcrossContext specificAcrossContext;

	@Autowired
	private NamingModule autoNamingModule;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private NamingModule currentModule;

	@Autowired
	@Qualifier("FirstModule")
	private AcrossModuleInfo moduleNamedFirst;

	@Autowired
	@Qualifier("LastModule")
	private AcrossModuleInfo moduleNamedLast;

	public AcrossContext getAutoAcrossContext() {
		return autoAcrossContext;
	}

	public AcrossContext getSpecificAcrossContext() {
		return specificAcrossContext;
	}

	public NamingModule getAutoNamingModule() {
		return autoNamingModule;
	}

	public NamingModule getCurrentModule() {
		return currentModule;
	}

	public NamingModule getModuleNamedFirst() {
		return (NamingModule) moduleNamedFirst.getModule();
	}

	public NamingModule getModuleNamedLast() {
		return (NamingModule) moduleNamedLast.getModule();
	}
}
