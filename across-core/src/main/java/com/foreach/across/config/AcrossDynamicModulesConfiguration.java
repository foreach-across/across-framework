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

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.core.context.SharedMetadataReaderFactory;
import com.foreach.across.core.util.ClassLoadingUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * Configuration that automatically adds a {@link AcrossDynamicModulesConfigurer} that uses the importing class
 * as a base for the dynamic modules.  And uses the shared {@link org.springframework.core.type.classreading.MetadataReaderFactory}
 * if it is available (bean named {@link SharedMetadataReaderFactory#BEAN_NAME}).
 *
 * @author Arne Vandamme
 * @see AcrossDynamicModulesConfigurer
 * @see AcrossApplication
 * @since 1.1.2
 */
@Configuration
@Slf4j
public class AcrossDynamicModulesConfiguration extends AcrossDynamicModulesConfigurer implements ImportAware, ApplicationContextAware
{
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		setResourcePatternResolver( applicationContext );
		if ( applicationContext.containsBean( SharedMetadataReaderFactory.BEAN_NAME ) ) {
			setMetadataReaderFactory( applicationContext.getBean( SharedMetadataReaderFactory.BEAN_NAME, MetadataReaderFactory.class ) );
		}
	}

	@Override
	public void setImportMetadata( AnnotationMetadata importMetadata ) {
		try {
			Class applicationClass = ClassLoadingUtils.loadClass( importMetadata.getClassName() );
			setApplicationClass( applicationClass );
			verifyNoConflictingComponentScans( importMetadata, getBasePackage() );
		}
		catch ( ClassNotFoundException cnfe ) {
			throw new AcrossConfigurationException( "Unable to configure dynamic application modules", cnfe );
		}
	}

	public static void verifyNoConflictingComponentScans( AnnotationMetadata importMetadata, String basePackage ) {
		Set<AnnotationAttributes> componentScans = buildComponentScans( importMetadata );

		if ( !componentScans.isEmpty() ) {
			LOG.warn( "" );
			LOG.warn( "--- Warning: Across Dynamic Modules @ComponentScan ---" );
			LOG.warn( "You are using @ComponentScan directly on a dynamic Across module importing class (eg. @AcrossApplication), this is not advised." );
			LOG.warn(
					"Avoid using @ComponentScan outside of an Across module as it's possible you might be scanning components that are also loaded inside a separate Across module." );
			LOG.warn( "Please see the Across documentation for more information on the Across context and modules." );
			LOG.warn( "--- End Warning: Across Dynamic Modules @ComponentScan ---" );
			LOG.warn( "" );

			for ( AnnotationAttributes componentScan : componentScans ) {
				if ( isDefaultComponentScan( componentScan ) ) {
					Set<String> scanPackages = buildScanPackages( importMetadata, componentScan );

					verifyNoScanConflict( scanPackages, basePackage + ".application" );
					verifyNoScanConflict( scanPackages, basePackage + ".infrastructure" );
					verifyNoScanConflict( scanPackages, basePackage + ".postprocessor" );
				}
			}
		}
	}

	private static void verifyNoScanConflict( Set<String> scanPackages, String basePackage ) {
		for ( String scanPackage : scanPackages ) {
			if ( scanPackage.startsWith( basePackage ) || basePackage.startsWith( scanPackage ) ) {
				AcrossConfigurationException configurationException = new AcrossConfigurationException(
						"Detected a @ComponentScan conflict between '" + scanPackage + "' and '" + basePackage + "'. " +
								"The latter is a dynamic module package and components should only be scanned within that module. "
				);
				configurationException.setActionToTake(
						"Remove the use of @ComponentScan on your @AcrossApplication or @EnableAcrossContext class. " +
								"Review your configuration and package layout, and ensure you do not scan any beans that are part of an Across module package. "
				);
				throw configurationException;
			}
		}
	}

	@SneakyThrows
	private static Set<String> buildScanPackages( AnnotationMetadata importMetadata, AnnotationAttributes componentScan ) {
		Set<String> packages = new HashSet<>();
		packages.addAll( Arrays.asList( componentScan.getStringArray( "basePackages" ) ) );
		packages.addAll( Arrays.asList( componentScan.getStringArray( "value" ) ) );
		Stream.of( componentScan.getClassArray( "basePackageClasses" ) )
		      .map( Class::getName )
		      .forEach( packages::add );

		if ( packages.isEmpty() ) {
			packages.add( ClassLoadingUtils.loadClass( importMetadata.getClassName() ).getPackage().getName() );
		}

		return packages;
	}

	private static boolean isDefaultComponentScan( AnnotationAttributes componentScan ) {
		String resourcePattern = componentScan.getString( "resourcePattern" );
		AnnotationAttributes[] includeFilters = componentScan.getAnnotationArray( "includeFilters" );
		AnnotationAttributes[] excludeFilters = componentScan.getAnnotationArray( "excludeFilters" );

		return DEFAULT_RESOURCE_PATTERN.equals( resourcePattern ) && includeFilters.length == 0 && excludeFilters.length == 0;
	}

	@SuppressWarnings("unchecked")
	private static Set<AnnotationAttributes> buildComponentScans( AnnotationMetadata importMetadata ) {
		Set<AnnotationAttributes> result = new LinkedHashSet<AnnotationAttributes>();
		addAttributesIfNotNull( result, importMetadata.getAnnotationAttributes( ComponentScan.class.getName(), false ) );

		Map<String, Object> container = importMetadata.getAnnotationAttributes( ComponentScans.class.getName(), false );
		if ( container != null && container.containsKey( "value" ) ) {
			for ( Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get( "value" ) ) {
				addAttributesIfNotNull( result, containedAttributes );
			}
		}
		return Collections.unmodifiableSet( result );
	}

	private static void addAttributesIfNotNull( Set<AnnotationAttributes> result, Map<String, Object> attributes ) {
		if ( attributes != null ) {
			result.add( AnnotationAttributes.fromMap( attributes ) );
		}
	}
}
