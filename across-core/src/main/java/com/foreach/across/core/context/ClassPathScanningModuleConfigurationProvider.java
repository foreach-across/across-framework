/*
 * Copyright 2019 the original author or authors
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
import com.foreach.across.core.context.module.ModuleConfigurationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
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
public class ClassPathScanningModuleConfigurationProvider extends AbstractClassPathScanningProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( ClassPathScanningCandidateModuleProvider.class );

	private static final String ANNOTATION_NAME = ModuleConfiguration.class.getName();

	public ClassPathScanningModuleConfigurationProvider( ResourcePatternResolver resourcePatternResolver ) {
		super( resourcePatternResolver );
	}

	public ClassPathScanningModuleConfigurationProvider( ResourcePatternResolver resourcePatternResolver,
	                                                     MetadataReaderFactory metadataReaderFactory ) {
		super( resourcePatternResolver, metadataReaderFactory );
	}

	public ModuleConfigurationSet scan( String... basePackages ) {
		ModuleConfigurationSet moduleConfigurationSet = new ModuleConfigurationSet();

		for ( String basePackage : basePackages ) {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath( basePackage ) + "/" + DEFAULT_RESOURCE_PATTERN;

			try {
				Resource[] resources = getResources( packageSearchPath );

				for ( Resource resource : resources ) {
					MetadataReader metadataReader = getMetadataReader( resource );
					AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

					if ( annotationMetadata.hasAnnotation( ANNOTATION_NAME ) ) {
						ClassMetadata classMetadata = metadataReader.getClassMetadata();

						if ( classMetadata.isConcrete() ) {
							Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes( ANNOTATION_NAME );
							String[] moduleNames = (String[]) attributes.get( "value" );

							ModuleConfigurationExtension extension = ModuleConfigurationExtension.of(
									classMetadata.getClassName(),
									(boolean) attributes.get( "deferred" ),
									(boolean) attributes.get( "optional" )
							);

							if ( moduleNames == null || moduleNames.length == 0 ) {
								moduleConfigurationSet.register( extension );
							}
							else {
								moduleConfigurationSet.register( extension, moduleNames );
							}

							String[] excludedModuleNames = (String[]) attributes.get( "exclude" );

							if ( excludedModuleNames != null && excludedModuleNames.length > 0 ) {
								moduleConfigurationSet.exclude( classMetadata.getClassName(), excludedModuleNames );
							}
						}
					}
				}
			}
			catch ( IOException ioe ) {
				LOG.error( "Unable to scan for @ModuleConfiguration classes - trying to continue", ioe );
			}
		}

		return moduleConfigurationSet;
	}
}
