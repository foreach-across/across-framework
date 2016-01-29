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
package com.foreach.across.core.context;

import com.foreach.across.core.annotations.ModuleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Scans a set of base packages to find all classes annotated with
 * {@link com.foreach.across.core.annotations.ModuleConfiguration}.
 *
 * @author Arne Vandamme
 */
public class ClassPathScanningModuleConfigurationProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( ClassPathScanningCandidateModuleProvider.class );

	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	private static final String ANNOTATION_NAME = ModuleConfiguration.class.getName();

	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			resourcePatternResolver );

	public ModuleConfigurationSet scan( String... basePackages ) {
		ModuleConfigurationSet moduleConfigurationSet = new ModuleConfigurationSet();

		for ( String basePackage : basePackages ) {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath( basePackage ) + "/" + DEFAULT_RESOURCE_PATTERN;

			try {
				Resource[] resources = resourcePatternResolver.getResources( packageSearchPath );

				for ( Resource resource : resources ) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader( resource );
					AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

					if ( annotationMetadata.hasAnnotation( ANNOTATION_NAME ) ) {
						ClassMetadata classMetadata = metadataReader.getClassMetadata();

						if ( classMetadata.isConcrete() ) {
							try {
								Class annotatedClass = Class.forName( classMetadata.getClassName() );

								Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes( ANNOTATION_NAME );
								String[] moduleNames = (String[]) attributes.get( "value" );

								if ( moduleNames == null || moduleNames.length == 0 ) {
									moduleConfigurationSet.register( annotatedClass );
								}
								else {
									moduleConfigurationSet.register( annotatedClass, moduleNames );
								}

								String[] excludedModuleNames = (String[]) attributes.get("exclude");

								if ( excludedModuleNames != null && excludedModuleNames.length > 0 ) {
									moduleConfigurationSet.exclude( annotatedClass, excludedModuleNames );
								}
							}
							catch ( ClassNotFoundException | IllegalStateException e ) {
								LOG.trace( "Unable to load @ModuleConfiguration class {}", classMetadata.getClassName(), e );
							}
						}
					}
				}
			}
			catch ( IOException ioe ) {
				LOG.warn( "Unable to scan for @ModuleConfiguration classes", ioe );
			}
		}
		return moduleConfigurationSet;
	}
}
