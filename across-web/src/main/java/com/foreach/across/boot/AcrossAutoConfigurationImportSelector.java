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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.stream.Collectors;
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
	public String[] selectImports( AnnotationMetadata annotationMetadata ) {
		AcrossApplicationAutoConfiguration registry = retrieveAutoConfigurationRegistry();

		return Stream.of( super.selectImports( annotationMetadata ) )
		             .filter( registry::requestAutoConfiguration )
		             .collect( Collectors.toList() )
		             .toArray( new String[0] );
	}

	private AcrossApplicationAutoConfiguration retrieveAutoConfigurationRegistry() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		AcrossApplicationAutoConfiguration registry = (AcrossApplicationAutoConfiguration) beanFactory.getSingleton(
				AcrossApplicationAutoConfiguration.class.getName() );

		if ( registry == null ) {
			registry = new AcrossApplicationAutoConfiguration();
			beanFactory.registerSingleton( AcrossApplicationAutoConfiguration.class.getName(), registry );
		}

		return registry;
	}
}
