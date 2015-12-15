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

import com.foreach.across.core.AcrossModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Based on {@link org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider}, this class
 * scans class path resources to detect valid {@link com.foreach.across.core.AcrossModule} implementations that could
 * be autoconfigured.  These must be concrete classes, with a public static NAME field and a parameter-less constructor.
 *
 * @author Arne Vandamme
 */
public class ClassPathScanningCandidateModuleProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( ClassPathScanningCandidateModuleProvider.class );

	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	private static final String MODULE_CLASS = AcrossModule.class.getName();

	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			resourcePatternResolver );

	public Map<String, Supplier<AcrossModule>> findCandidateModules( String... basePackages ) {
		Map<String, Supplier<AcrossModule>> candidates = new HashMap<>();

		for ( String basePackage : basePackages ) {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					ClassUtils.convertClassNameToResourcePath( basePackage ) + "/" + DEFAULT_RESOURCE_PATTERN;

			try {
				Resource[] resources = resourcePatternResolver.getResources( packageSearchPath );

				for ( Resource resource : resources ) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader( resource );
					ClassMetadata classMetadata = metadataReader.getClassMetadata();

					if ( isAcrossModuleClass( classMetadata ) ) {
						try {
							Class moduleClass = Class.forName( classMetadata.getClassName() );

							if ( hasParameterlessConstructor( moduleClass ) ) {
								String moduleName = retrieveModuleName( moduleClass );

								if ( moduleName != null ) {
									LOG.trace( "Registering module {} for auto-configuration, type {}",
									           moduleName, moduleClass.getName() );

									candidates.put( moduleName, () -> {
										try {
											return (AcrossModule) moduleClass.newInstance();
										}
										catch ( Exception e ) {
											LOG.error( "Exception instantiating module {}", moduleName, e );
											return null;
										}
									} );
								}
								else {
									LOG.trace(
											"Unable to autoconfigure module of type {} as there is no public static final NAME field",
											moduleClass
									);
								}
							}
							else {
								LOG.trace(
										"Unable to autoconfigure module of type {} as it has no public parameter-less constructor",
										moduleClass
								);
							}
						}
						catch ( ClassNotFoundException | IllegalStateException e ) {
							LOG.trace( "Unable to determine module name for {}", classMetadata.getClassName(), e );
						}
					}
				}
			}
			catch ( IOException ioe ) {
				LOG.warn( "Unable to scan for module classes", ioe );
			}
		}

		return candidates;
	}

	private String retrieveModuleName( Class moduleClass ) {
		Field nameField = ReflectionUtils.findField( moduleClass, "NAME" );

		if ( nameField != null ) {
			try {
				return (String) nameField.get( moduleClass );
			}
			catch ( IllegalAccessException iae ) {
				return null;
			}
		}

		return null;
	}

	protected boolean hasParameterlessConstructor( Class<?> moduleClass ) {
		try {
			Constructor constructor = moduleClass.getConstructor();

			if ( Modifier.isPublic( constructor.getModifiers() ) ) {
				return true;
			}
		}
		catch ( Exception ignore ) {
		}

		return false;
	}

	protected boolean isAcrossModuleClass( ClassMetadata classMetadata ) {
		if ( classMetadata.isConcrete() && classMetadata.hasSuperClass() ) {
			String superClassName = classMetadata.getSuperClassName();

			if ( StringUtils.equals( MODULE_CLASS, superClassName ) ) {
				return true;
			}
			else {
				try {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader( superClassName );
					ClassMetadata parentClassMetadata = metadataReader.getClassMetadata();

					return isAcrossModuleClass( parentClassMetadata );
				}
				catch ( IOException ioe ) {
					return false;
				}
			}
		}

		return false;
	}
}
