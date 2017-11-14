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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider that finds direct child packages for a given package.
 *
 * @author Arne Vandamme
 */
public class ClassPathScanningChildPackageProvider extends AbstractClassPathScanningProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( ClassPathScanningChildPackageProvider.class );

	private final Set<String> excluded = new HashSet<>();

	public ClassPathScanningChildPackageProvider( ResourcePatternResolver resourcePatternResolver ) {
		super( resourcePatternResolver );
	}

	public ClassPathScanningChildPackageProvider( ResourcePatternResolver resourcePatternResolver,
	                                              MetadataReaderFactory metadataReaderFactory ) {
		super( resourcePatternResolver, metadataReaderFactory );
	}

	/**
	 * Set one or more child package names that should be excluded.
	 *
	 * @param packageNames names of the direct child
	 */
	public void setExcludedChildPackages( String... packageNames ) {
		excluded.clear();
		Collections.addAll( excluded, packageNames );
	}

	/**
	 * Find direct child packages that are not being excluded.
	 *
	 * @param basePackage root package
	 * @return fully qualified child package names
	 */
	public String[] findChildren( String basePackage ) {
		Set<String> packageNames = new LinkedHashSet<>();

		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath( basePackage ) + "/" + DEFAULT_RESOURCE_PATTERN;

		try {
			Resource[] resources = getResources( packageSearchPath );

			for ( Resource resource : resources ) {
				MetadataReader metadataReader = getMetadataReader( resource );
				ClassMetadata classMetadata = metadataReader.getClassMetadata();

				String packageName = StringUtils.substringBeforeLast( classMetadata.getClassName(), "." );
				if ( !StringUtils.equals( basePackage, packageName ) ) {
					String child = StringUtils.substringBefore(
							StringUtils.replaceOnce( packageName, basePackage + ".", "" ),
							"."
					);

					if ( !excluded.contains( child ) ) {
						packageNames.add( basePackage + "." + child );
					}
				}

			}
		}
		catch ( IOException ioe ) {
			LOG.warn( "Unable to scan for child packages", ioe );
		}

		return packageNames.toArray( new String[packageNames.size()] );
	}
}
