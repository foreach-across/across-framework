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

import com.foreach.across.config.AcrossApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Defers the actual import selection to Across.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class AcrossAutoConfigurationImportSelector extends AutoConfigurationImportSelector
{
	@Override
	protected Class<?> getAnnotationClass() {
		return AcrossApplication.class;
	}

	@Override
	public String[] selectImports( AnnotationMetadata annotationMetadata ) {
		String[] sortedOriginalImports = super.selectImports( annotationMetadata );

		AcrossApplicationAutoConfiguration registry = retrieveAutoConfigurationRegistry();
		AnnotationAttributes attributes = getAttributes( annotationMetadata );
		String[] excludedAutoConfigurations = attributes.getStringArray( "excludeAutoConfigurations" );
		registry.addExcludedAutoConfigurations( excludedAutoConfigurations );

		String[] actualImports = Stream.of( sortedOriginalImports )
		                               .map( registry::requestAutoConfiguration )
		                               .filter( Objects::nonNull )
		                               .filter( registry::notExcluded )
		                               .toArray( String[]::new );

		registry.printAutoConfigurationReport();

		return actualImports;
	}

	@Override
	protected boolean isEnabled( AnnotationMetadata metadata ) {
		AnnotationAttributes attributes = getAttributes( metadata );
		return attributes.getBoolean( "autoConfiguration" );
	}

	@Override
	protected Set<String> getExclusions( AnnotationMetadata metadata, AnnotationAttributes attributes ) {
		AcrossApplicationAutoConfiguration registry = retrieveAutoConfigurationRegistry();

		attributes.put( "exclude", new String[0] );
		attributes.put( "excludeName", new String[0] );

		Set<String> exclusions = super.getExclusions( metadata, attributes );
		exclusions.forEach( registry::addExcludedAutoConfigurations );

		return exclusions;
	}

	private AcrossApplicationAutoConfiguration retrieveAutoConfigurationRegistry() {
		return AcrossApplicationAutoConfiguration.retrieve( getBeanFactory(), getBeanClassLoader() );
	}
}
