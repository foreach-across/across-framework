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
package com.foreach.across.core.annotations;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Extension of {@link org.springframework.context.annotation.Configuration} to declare configurations
 * that should not be added to the current {@link org.springframework.beans.factory.support.BeanDefinitionRegistry},
 * but rather be added to the registry of the modules specified in the {@link #value()} attribute.
 *
 * @author Arne Vandamme
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface ModuleConfiguration
{
	/**
	 * Should this additional configuration be added after the initial module configuration ({@code true})
	 * or before ({@code false}). Default is {@code true} so extension configurations can override beandefinitions
	 * or act on conditional creation from the original module config.
	 */
	boolean deferred() default true;

	/**
	 * List of module names where this configuration should be imported.
	 * If the list is empty this configuration will be added to all modules.
	 */
	String[] value() default {};

	/**
	 * List of module names where this configuration should not be imported.
	 * Takes precedence over {@link #value()} and is mainly useful if {@link #value()} is empty which
	 * would imply the configuration would be added to all modules (including the module providing the configuration).
	 */
	String[] exclude() default {};

	/**
	 * Explicitly specify the name of the Spring bean definition associated
	 * with this Configuration class. If left unspecified (the common case),
	 * a bean name will be automatically generated.
	 *
	 * @return the suggested component name, if any (or empty String otherwise)
	 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
	 */
	@AliasFor(annotation = Configuration.class, attribute = "value")
	String beanName() default "";
}
