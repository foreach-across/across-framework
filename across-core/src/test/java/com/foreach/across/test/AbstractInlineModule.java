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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Set;

public abstract class AbstractInlineModule extends AcrossModule
{
	private final String name;

	protected AbstractInlineModule( String name, Class... annotatedClasses ) {
		this.name = name;
		addApplicationContextConfigurer( new AnnotatedClassConfigurer( annotatedClasses ) );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return "inline test module";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
	}
}
