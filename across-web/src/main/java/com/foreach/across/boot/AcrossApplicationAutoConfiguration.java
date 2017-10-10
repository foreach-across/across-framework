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
package com.foreach.across.boot;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class AcrossApplicationAutoConfiguration
{
	public enum Scope {
		Application,
		AcrossContext,
		AcrossModule
	}

	private Set<String> excluded = new HashSet<>();
	private Set<String> requested = new HashSet<>();

	public AcrossApplicationAutoConfiguration() {
		exclude( LiquibaseAutoConfiguration.class.getName() );
	}

	public void exclude( String autoConfigurationClass ) {
		excluded.add( autoConfigurationClass );
	}

	public boolean requestAutoConfiguration( String autoConfigurationClass ) {
		requested.add( autoConfigurationClass );

		if ( WebMvcAutoConfiguration.class.getName().equals( autoConfigurationClass )) {
			return true;
		}

		return false;
	}
}
