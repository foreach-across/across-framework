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

import com.foreach.across.config.AcrossConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

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

	private final Set<String> excluded = new LinkedHashSet<>();

	private final Map<String, String> allowed = new LinkedHashMap<>();

	private final Map<String, ModuleExtension> extendModules = new LinkedHashMap<>();
	private final Set<String> requested = new LinkedHashSet<>();
	private final Map<String, Integer> autoConfigurationOrder = new HashMap<>();
	private final Set<String> unknownSupport = new LinkedHashSet<>();

	public AcrossApplicationAutoConfiguration( ClassLoader classLoader ) {
		AcrossConfiguration
				.get( classLoader )
				.getAutoConfigurationClasses()
				.forEach( autoConfigurationClass -> {
					if ( autoConfigurationClass.isDisabled() ) {
						excluded.add( autoConfigurationClass.getClassName() );
					}
					else {
						if ( autoConfigurationClass.hasDestinationModule() ) {
							extendModules.put(
									autoConfigurationClass.getClassName(),
									new ModuleExtension( autoConfigurationClass.getDestinationModule(), autoConfigurationClass.getConfigurationClassName() )
							);
						}
						else {
							allowed.put( autoConfigurationClass.getClassName(), autoConfigurationClass.getConfigurationClassName() );
						}
					}
				} );
	}

	public void addExcludedAutoConfigurations( String... excludedConfigurationClasses ) {
		excluded.addAll( Arrays.asList( excludedConfigurationClasses ) );
	}

	public String requestAutoConfiguration( String autoConfigurationClass ) {
		if ( requested.add( autoConfigurationClass ) ) {
			autoConfigurationOrder.put( autoConfigurationClass, requested.size() );
		}

		String actualClass = allowed.get( autoConfigurationClass );
		ModuleExtension moduleExtension = extendModules.get( autoConfigurationClass );
		boolean disabled = excluded.contains( autoConfigurationClass );

		if ( actualClass == null && !disabled && moduleExtension == null ) {
			unknownSupport.add( autoConfigurationClass );
		}

		if ( actualClass != null && !autoConfigurationClass.equals( actualClass ) ) {
			LOG.trace( "Resolved AutoConfiguration class {} to {} adapter", autoConfigurationClass, actualClass );
		}

		if ( disabled ) {
			LOG.trace( "Disallowed AutoConfiguration class {}", autoConfigurationClass );
		}

		if ( moduleExtension != null ) {
			LOG.trace( "Resolved AutoConfiguration class {} to extend module {} with {}", autoConfigurationClass, moduleExtension.moduleName,
			           moduleExtension.className );
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
		                .map( extendModules::get )
		                .filter( Objects::nonNull )
		                .collect( Collectors.groupingBy( ModuleExtension::getModuleName, mapping( ModuleExtension::getClassName, toList() ) ) );
	}

	public Integer getAutoConfigurationOrder( String className ) {
		return autoConfigurationOrder.getOrDefault( className, Ordered.LOWEST_PRECEDENCE );
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

	@Getter
	@RequiredArgsConstructor
	private static class ModuleExtension
	{
		private final String moduleName;
		private final String className;
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
