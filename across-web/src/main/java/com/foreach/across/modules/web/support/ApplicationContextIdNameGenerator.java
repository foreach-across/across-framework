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
package com.foreach.across.modules.web.support;

import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

/**
 * A utility class that generates an easily readable String for use in
 *
 * @author Marc Vanbrabant
 * @see org.springframework.context.support.AbstractRefreshableConfigApplicationContext#setId(String)
 */
public abstract class ApplicationContextIdNameGenerator
{
	private static final String PREFIX = "[AX";
	private static final String SUFFIX = "] ";

	public static String forModule( ModuleBootstrapConfig moduleBootstrapConfig ) {
		return PREFIX + "_" + StringUtils.leftPad( String.valueOf( moduleBootstrapConfig.getBootstrapIndex() ), 2, "0" ) + SUFFIX + moduleBootstrapConfig
				.getModuleName();
	}

	public static String forContext( ApplicationContext applicationContext ) {
		if ( StringUtils.equals( "Root WebApplicationContext", applicationContext.getDisplayName() ) ) {
			return PREFIX + SUFFIX + "# " + applicationContext.getDisplayName();
		}
		else {
			return PREFIX + SUFFIX + applicationContext.getDisplayName();
		}
	}
}
