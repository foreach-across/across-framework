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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Extends the default {@link AnnotationBeanNameGenerator} to always use a fully qualified class name
 * as bean name in case of a {@link Configuration} or {@link ModuleConfiguration} class, as well
 * as for any class not annotated with {@link Component} (which is in fact the case for {@link ModuleConfiguration}).
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class ModuleConfigurationBeanNameGenerator extends AnnotationBeanNameGenerator
{
	private static final String COMPONENT_ANNOTATION = Component.class.getName();
	private static final String CONFIGURATION_ANNOTATION = Configuration.class.getName();

	@Override
	protected String determineBeanNameFromAnnotation( AnnotatedBeanDefinition annotatedDef ) {
		String name = super.determineBeanNameFromAnnotation( annotatedDef );

		if ( name == null ) {
			AnnotationMetadata metadata = annotatedDef.getMetadata();
			if ( !metadata.isAnnotated( COMPONENT_ANNOTATION ) || metadata.isAnnotated( CONFIGURATION_ANNOTATION ) ) {
				return ClassUtils.getPackageName( annotatedDef.getBeanClassName() ) + "." + ClassUtils.getShortName( annotatedDef.getBeanClassName() );
			}
		}

		return name;
	}
}
