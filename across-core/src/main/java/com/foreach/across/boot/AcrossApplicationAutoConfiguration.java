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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks requested auto-configuration classes and determines which action should be taken:
 * - allow the auto-configuration class (on the application level)
 * - inject the auto-configuration class in another module instead
 * - inject another configuration class instead of the one requested
 * - simply reject it
 * <p/>
 * Checks the across.configuration file for the auto-configuration class rules:
 * - com.foreach.across.AutoConfigurationDisabled contains the classes to reject
 * - com.foreach.across.AutoConfigurationEnabled contains the classes allowed and their corresponding actions
 * <p/>
 * Possible entries for AutoConfigurationEnabled:
 * - A will allow class A on the application level
 * - A:B will add class B on the application level when A is requested
 * - A->ModuleName will inject class A in module ModuleName, when A is requested
 * <p/>
 * Note: once a class has been excluded using AutoConfigurationDisabled, it will never be added automatically,
 * even if there are AutoConfigurationEnabled rules.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public final class AcrossApplicationAutoConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossApplicationAutoConfiguration.class );

	public static final String ENABLED_AUTO_CONFIGURATION = "com.foreach.across.AutoConfigurationEnabled";
	public static final String DISABLED_AUTO_CONFIGURATION = "com.foreach.across.AutoConfigurationDisabled";

	private final Set<String> excluded = new HashSet<>();

	private final Map<String, String> allowed = new HashMap<>();

	private final Map<String, String> extendModules = new HashMap<>();
	private final Set<String> requested = new HashSet<>();
	private final Set<String> unknownSupport = new LinkedHashSet<>();

	public AcrossApplicationAutoConfiguration( ClassLoader classLoader ) {
		AcrossConfigurationLoader
				.loadValues( ENABLED_AUTO_CONFIGURATION, classLoader )
				.forEach( this::registerEnabledAutoConfiguration );
		excluded.addAll( AcrossConfigurationLoader.loadValues( DISABLED_AUTO_CONFIGURATION, classLoader ) );
	}

	private void registerEnabledAutoConfiguration( String classTransform ) {
		if ( classTransform.contains( ":" ) ) {
			String[] parts = classTransform.split( ":" );
			allowed.put( parts[0], parts[1] );
		}
		else if ( classTransform.contains( "->" ) ) {
			String[] parts = classTransform.split( "->" );
			extendModules.put( parts[0], parts[1] );
		}
		else {
			allowed.put( classTransform, classTransform );
		}
	}

	public void addExcludedAutoConfigurations( String... excludedConfigurationClasses ) {
		excluded.addAll( Arrays.asList( excludedConfigurationClasses ) );
	}

	public String requestAutoConfiguration( String autoConfigurationClass ) {
		requested.add( autoConfigurationClass );

		String actualClass = allowed.get( autoConfigurationClass );
		String extendModule = extendModules.get( autoConfigurationClass );
		boolean disabled = excluded.contains( autoConfigurationClass );

		if ( actualClass == null && !disabled && extendModule == null ) {
			unknownSupport.add( autoConfigurationClass );
		}

		if ( actualClass != null && !autoConfigurationClass.equals( actualClass ) ) {
			LOG.trace( "Resolved AutoConfiguration class {} to {} adapter", autoConfigurationClass, actualClass );
		}

		if ( disabled ) {
			LOG.trace( "Disallowed AutoConfiguration class {}", autoConfigurationClass );
		}

		if ( extendModule != null ) {
			LOG.trace( "Resolved AutoConfiguration class {} to extend module {}", autoConfigurationClass, extendModule );
			return null;
		}

		return disabled ? null : actualClass;
	}

	public boolean notExcluded( String className ) {
		return !excluded.contains( className );
	}

	public Map<String, List<String>> getModuleExtensions() {
		return requested.stream()
		                .filter( this::notExcluded )
		                .filter( extendModules::containsKey )
		                .collect( Collectors.groupingBy( extendModules::get ) );
	}

	void printAutoConfigurationReport() {
		if ( !unknownSupport.isEmpty() ) {
			LOG.warn( "" );
			LOG.warn( "--- Across AutoConfiguration Report ---" );
			LOG.warn( "The following {} auto-configuration classes have unknown Across support and were not added:", unknownSupport.size() );
			unknownSupport.forEach( className -> LOG.warn( "- {}", className ) );
			LOG.warn( "Consider adding them to a META-INF/across.configuration." );
			LOG.warn( "--- End Across AutoConfiguration Report ---" );
			LOG.warn( "" );
		}
	}

	/**
	 * Retrieve (or register) the single instance of the Across auto-configuration.
	 *
	 * @param beanFactory to request for the instance
	 * @param classLoader to use when creating
	 * @return instance
	 */
	public static AcrossApplicationAutoConfiguration retrieve( ConfigurableListableBeanFactory beanFactory, ClassLoader classLoader ) {
		if ( !beanFactory.containsBean( AcrossApplicationAutoConfiguration.class.getName() ) ) {
			AcrossApplicationAutoConfiguration registry = new AcrossApplicationAutoConfiguration( classLoader );
			beanFactory.registerSingleton( AcrossApplicationAutoConfiguration.class.getName(), registry );
			return registry;
		}

		return beanFactory.getBean( AcrossApplicationAutoConfiguration.class.getName(), AcrossApplicationAutoConfiguration.class );
	}
}
