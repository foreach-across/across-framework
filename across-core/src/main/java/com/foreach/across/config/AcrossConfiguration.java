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
package com.foreach.across.config;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Utility loader for retrieving data from <strong>META-INF/across.configuration</strong> file.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AcrossConfiguration
{
	/**
	 * Location to look for configuration file (in multiple jars).
	 */
	public static final String CONFIGURATION_RESOURCE_LOCATION = "META-INF/across-configuration.yml";

	private static final int DEFAULT_PRIORITY = 1000;

	private static final Yaml YAML = new Yaml();
	private static final Map<ClassLoader, AcrossConfiguration> CONFIGURATIONS = new HashMap<>();

	@Getter
	private final List<Group> groups;

	private transient Collection<AutoConfigurationClass> autoConfigurationClasses;

	/**
	 * Fetch the first group with that name.
	 *
	 * @param groupName name of the group
	 * @return group or {@code null} if none
	 */
	public Group getGroup( String groupName ) {
		for ( Group g : groups ) {
			if ( StringUtils.equals( groupName, g.getName() ) ) {
				return g;
			}
		}
		return null;
	}

	public Collection<String> getExposeRules() {
		return groups.stream()
		             .flatMap( group -> group.getExposeRules().stream() )
		             .collect( Collectors.toSet() );
	}

	/**
	 * @return unique set of auto-configuration class rules
	 */
	public Collection<AutoConfigurationClass> getAutoConfigurationClasses() {
		if ( autoConfigurationClasses == null ) {
			Map<String, AutoConfigurationClass> classMap = new HashMap<>();

			groups.stream()
			      .flatMap( group -> group.getAutoConfigurationClasses().stream() )
			      .forEach( c -> classMap.putIfAbsent( c.getClassName(), c ) );

			autoConfigurationClasses = classMap.values();
		}

		return autoConfigurationClasses;
	}

	/**
	 * @return flat set of illegal configuration
	 */
	public Collection<IllegalConfiguration> getIllegalConfigurations() {
		return groups.stream()
		             .flatMap( group -> group.getIllegalConfigurations().stream() )
		             .collect( Collectors.toList() );
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	@EqualsAndHashCode
	@ToString
	public static class Group
	{
		private final int priority;
		private final String name;
		private final Set<String> exposeRules;
		private final List<AutoConfigurationClass> autoConfigurationClasses;
		private final List<IllegalConfiguration> illegalConfigurations;
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	@EqualsAndHashCode
	@ToString
	public static class AutoConfigurationClass
	{
		private final String className;
		private final boolean enabled;
		private final String destinationModule;
		private final String adapterClassName;

		public boolean isDisabled() {
			return !isEnabled();
		}

		public boolean hasDestinationModule() {
			return destinationModule != null;
		}

		/**
		 * @return the actual class name that should be used as configuration class, either the original or the adapter
		 */
		public String getConfigurationClassName() {
			return adapterClassName != null ? adapterClassName : className;
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	@EqualsAndHashCode
	@ToString
	public static class IllegalConfiguration
	{
		private final String name;
		private final String description;
		private final String action;
		private final Collection<ClassEntry> configurations;

		@Getter
		@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
		@EqualsAndHashCode
		@ToString
		public static class ClassEntry
		{
			private final String className;
			private final String allowed;
			private final String illegal;
		}
	}

	/**
	 * Retrieve the AcrossConfiguration for the specified class loader.
	 * Will parse all configuration files that can be found in the class loader.
	 *
	 * @param classLoader instance
	 * @return configuration instance, never {@code null}
	 */
	public static AcrossConfiguration get( ClassLoader classLoader ) {
		return CONFIGURATIONS.computeIfAbsent( classLoader, AcrossConfiguration::buildConfiguration );
	}

	/**
	 * Remove the AcrossConfiguration attached to the class loader.
	 *
	 * @param classLoader instance
	 */
	public static void remove( ClassLoader classLoader ) {
		CONFIGURATIONS.remove( classLoader );
	}

	@SuppressWarnings("unchecked")
	private static AcrossConfiguration buildConfiguration( ClassLoader classLoader ) {
		List<Group> groups = new ArrayList<>();

		try {
			Enumeration<URL> urls = ( classLoader != null ? classLoader.getResources( CONFIGURATION_RESOURCE_LOCATION ) :
					ClassLoader.getSystemResources( CONFIGURATION_RESOURCE_LOCATION ) );

			while ( urls.hasMoreElements() ) {
				URL url = urls.nextElement();

				try (InputStream is = url.openStream()) {
					Map<?, ?> data = YAML.load( is );

					data.forEach( ( groupName, entries ) -> {
						groups.add( buildGroup( groupName, (Map) entries ) );
					} );
				}
			}
		}
		catch ( IOException ex ) {
			throw new RuntimeException( "Unable to load across-configuration.yml", ex );
		}

		groups.sort( Comparator.comparingInt( Group::getPriority ).reversed() );

		return new AcrossConfiguration( groups );
	}

	@SuppressWarnings("unchecked")
	private static Group buildGroup( Object key, Map<Object, Object> data ) {
		String groupName = key.toString();
		int priority = (int) data.getOrDefault( "priority", DEFAULT_PRIORITY );

		Set<String> exposedRules = new HashSet<>();
		Object exposed = data.get( "exposed" );
		if ( exposed != null ) {
			for ( Object c : (Collection) exposed ) {
				exposedRules.add( c.toString() );
			}
		}

		List<AutoConfigurationClass> autoConfigurationClasses = new ArrayList<>();
		Object autoConfigurations = data.get( "auto-configuration" );
		if ( autoConfigurations != null ) {
			( (Map) autoConfigurations ).forEach( ( k, v ) -> autoConfigurationClasses.add( buildAutoConfigurationClass( k.toString(), v ) ) );
		}

		List<IllegalConfiguration> illegalConfigurations = new ArrayList<>();
		Object illegals = data.get( "illegal-configurations" );
		if ( illegals != null ) {
			( (Map) illegals ).forEach( ( k, v ) -> illegalConfigurations.add( buildIllegalConfiguration( k.toString(), (Map) v ) ) );
		}

		return new Group(
				priority, groupName, unmodifiableSet( exposedRules ), unmodifiableList( autoConfigurationClasses ), unmodifiableList( illegalConfigurations )
		);
	}

	@SuppressWarnings("unchecked")
	private static IllegalConfiguration buildIllegalConfiguration( String name, Map data ) {
		List<IllegalConfiguration.ClassEntry> configurations = new ArrayList<>();
		Map entries = (Map) data.get( "configuration" );

		if ( entries != null ) {
			entries.forEach( ( className, rules ) -> {
				String allowed = rules != null ? Objects.toString( ( (Map) rules ).get( "allowed" ), null ) : null;
				String illegal = rules != null ? Objects.toString( ( (Map) rules ).get( "illegal" ), null ) : null;

				if ( allowed == null && illegal == null ) {
					illegal = "AcrossContext";
				}

				configurations.add( new IllegalConfiguration.ClassEntry( className.toString(), allowed, illegal ) );
			} );
		}

		return new IllegalConfiguration( name,
		                                 Objects.toString( data.get( "description" ), "" ),
		                                 Objects.toString( data.get( "action" ), "" ),
		                                 Collections.unmodifiableList( configurations ) );
	}

	@SuppressWarnings("unchecked")
	private static AutoConfigurationClass buildAutoConfigurationClass( String className, Object rule ) {
		boolean disabled = false;
		String adapterClassName = null;
		String destinationModuleName = null;

		if ( rule != null ) {
			if ( rule instanceof Map ) {
				Map data = (Map) rule;
				disabled = isFalse( data.get( "enabled" ) );
				adapterClassName = (String) data.get( "adapter" );
				destinationModuleName = (String) data.get( "destination" );
			}
			else {
				disabled = isFalse( rule );
			}
		}

		return new AutoConfigurationClass( className, !disabled, destinationModuleName, adapterClassName );
	}

	private static boolean isFalse( Object value ) {
		return Boolean.FALSE.equals( value ) || new Integer( 0 ).equals( value );
	}
}
