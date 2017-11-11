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

import com.foreach.across.config.AcrossConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class AcrossApplicationAutoConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossApplicationAutoConfiguration.class );

	public static final String ENABLED_AUTO_CONFIGURATION = "com.foreach.across.AutoConfigurationEnabled";
	public static final String DISABLED_AUTO_CONFIGURATION = "com.foreach.across.AutoConfigurationDisabled";

	public boolean notExcluded( String className ) {
		return !excluded.contains( className );
	}

	public enum Scope
	{
		Application,
		AcrossContext,
		AcrossModule;
	}

	private final Set<String> excluded = new HashSet<>();

	private final Map<String, String> allowed = new HashMap<>();
	private final Set<String> requested = new HashSet<>();
	private final Set<String> unknownSupport = new LinkedHashSet<>();

	public AcrossApplicationAutoConfiguration( ClassLoader classLoader ) {
		allowed.putAll( AcrossConfigurationLoader.loadMapValues( ENABLED_AUTO_CONFIGURATION, classLoader ) );
		excluded.addAll( AcrossConfigurationLoader.loadValues( DISABLED_AUTO_CONFIGURATION, classLoader ) );
	}

	public String requestAutoConfiguration( String autoConfigurationClass ) {
		requested.add( autoConfigurationClass );

		String actualClass = allowed.get( autoConfigurationClass );
		boolean disabled = excluded.contains( autoConfigurationClass );

		if ( actualClass == null && !disabled ) {
			unknownSupport.add( autoConfigurationClass );
		}

		if ( actualClass != null && !autoConfigurationClass.equals( actualClass ) ) {
			LOG.trace( "Resolved AutoConfiguration class {} to {} adapter", autoConfigurationClass, actualClass );
		}

		if ( disabled ) {
			LOG.trace( "Disallowed AutoConfiguration class {}", autoConfigurationClass );
		}

		return disabled ? null : actualClass;
	}

	void printAutoConfigurationReport() {
		if ( !unknownSupport.isEmpty() ) {
			LOG.warn( "" );
			LOG.warn( "--- Across AutoConfiguration Report ---" );
			LOG.warn( "The following auto-configuration classes have unknown Across support and were not added:" );
			unknownSupport.forEach( className -> LOG.warn( "- {}", className ) );
			 LOG.warn( "Consider adding them to a META-INF/across.configuration." );
			LOG.warn( "--- End Across AutoConfiguration Report ---" );
			LOG.warn( "" );
		}
	}
}
