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

import lombok.val;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Conditional to check that an auto-configuration class has been configured in the Across application.
 * Does not check where it has been loaded, this can be either on the application itself, or as a module extension.
 * <p/>
 * Condition will only match if all configuration classes are present.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ConditionalOnAutoConfiguration.AutoConfigurationCondition.class)
public @interface ConditionalOnAutoConfiguration
{
	/**
	 * @return auto-configuration class names
	 */
	String[] classNames() default {};

	/**
	 * @return auto-configuration class names
	 */
	@AliasFor("value")
	Class<?>[] classes() default {};

	/**
	 * @return auto-configuration class names
	 */
	@AliasFor("classes")
	Class<?>[] value() default {};

	class AutoConfigurationCondition implements Condition
	{
		@Override
		public boolean matches( ConditionContext context, AnnotatedTypeMetadata metadata ) {
			AnnotationAttributes attributes = (AnnotationAttributes) metadata.getAnnotationAttributes( ConditionalOnAutoConfiguration.class.getName(), true );
			val registry = AcrossApplicationAutoConfiguration.retrieve( context.getBeanFactory(), context.getClassLoader() );

			List<String> required = new ArrayList<>();
			String[] classNames = attributes.getStringArray( "value" );
			if ( classNames != null ) {
				required.addAll( Arrays.asList( classNames ) );
			}
			classNames = attributes.getStringArray( "classNames" );
			if ( classNames != null ) {
				required.addAll( Arrays.asList( classNames ) );
			}

			for ( String className : required ) {
				if ( !registry.notExcluded( className ) ) {
					return false;
				}
			}

			return true;
		}
	}
}
