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

package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.InstallerMethod;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class TestInstaller
{
	public static List<Class<?>> EXECUTED = new LinkedList<>();
	public static Map<Class<?>, String> WIRED_VALUES = new HashMap<>();

	@Autowired(required = false)
	public void setSomeBean( String someBean ) {
		WIRED_VALUES.put( getClass(), someBean );
	}

	@InstallerMethod
	public void run() {
		EXECUTED.add( getClass() );
	}

	public static Class[] executed() {
		return EXECUTED.toArray( new Class[EXECUTED.size()] );
	}

	public static void reset() {
		EXECUTED.clear();
		WIRED_VALUES.clear();
	}
}
