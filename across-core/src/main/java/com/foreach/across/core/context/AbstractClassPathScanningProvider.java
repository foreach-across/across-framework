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

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

/**
 * Base class for {@link org.springframework.core.type.classreading.MetadataReaderFactory}
 * and {@link org.springframework.core.io.support.ResourcePatternResolver}.
 *
 * @author Arne Vandamme
 * @since 2.1.1
 */
public abstract class AbstractClassPathScanningProvider
{
	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	private final ResourcePatternResolver resourcePatternResolver;
	private final MetadataReaderFactory metadataReaderFactory;

	public AbstractClassPathScanningProvider() {
		this( new PathMatchingResourcePatternResolver() );
	}

	public AbstractClassPathScanningProvider( ResourcePatternResolver resourcePatternResolver ) {
		this( resourcePatternResolver, new CachingMetadataReaderFactory( resourcePatternResolver ) );
	}

	public AbstractClassPathScanningProvider( ResourcePatternResolver resourcePatternResolver,
	                                          MetadataReaderFactory metadataReaderFactory ) {
		this.resourcePatternResolver = resourcePatternResolver;
		this.metadataReaderFactory = metadataReaderFactory;
	}

	protected Resource[] getResources( String locationPattern ) throws IOException {
		return resourcePatternResolver.getResources( locationPattern );
	}

	protected MetadataReader getMetadataReader( Resource resource ) throws IOException {
		return metadataReaderFactory.getMetadataReader( resource );
	}

	protected MetadataReader getMetadataReader( String className ) throws IOException {
		return metadataReaderFactory.getMetadataReader( className );
	}

	public void clearCache() {
		if ( metadataReaderFactory instanceof CachingMetadataReaderFactory ) {
			( (CachingMetadataReaderFactory) metadataReaderFactory ).clearCache();
		}
	}
}
