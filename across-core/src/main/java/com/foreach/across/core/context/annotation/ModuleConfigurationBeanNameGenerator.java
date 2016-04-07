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
package com.foreach.across.core.context.annotation;

import com.foreach.across.core.annotations.ModuleConfiguration;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

import java.beans.Introspector;
import java.util.Set;

/**
 * Extends the default {@link AnnotationBeanNameGenerator} with support for
 * {@link com.foreach.across.core.annotations.ModuleConfiguration} classes.
 * For the latter the full qualified class name will be used for default bean name,
 * instead of the simple name.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class ModuleConfigurationBeanNameGenerator extends AnnotationBeanNameGenerator
{
	@Override
	protected String determineBeanNameFromAnnotation( AnnotatedBeanDefinition annotatedDef ) {
		String name = super.determineBeanNameFromAnnotation( annotatedDef );

		if ( name == null ) {
			AnnotationMetadata amd = annotatedDef.getMetadata();
			Set<String> types = amd.getAnnotationTypes();

			if ( types.contains( ModuleConfiguration.class.getName() ) ) {
				// use fully qualified class name as default bean name
				return Introspector.decapitalize( annotatedDef.getBeanClassName() );
			}
		}

		return name;
	}
}
