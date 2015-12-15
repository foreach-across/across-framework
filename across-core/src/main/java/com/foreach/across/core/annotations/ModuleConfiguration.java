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
package com.foreach.across.core.annotations;

import java.lang.annotation.*;

/**
 * Alternative for {@link org.springframework.context.annotation.Configuration} to declare annotated classes
 * that should not be added to the current {@link org.springframework.beans.factory.support.BeanDefinitionRegistry},
 * but rather be added to the registry of the modules specified in the {@link #value()} attribute.
 *
 * @author Arne Vandamme
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModuleConfiguration
{
	/**
	 * List of module names where this configuration should be imported.
	 * If the list is empty this configuration will be added to all modules.
	 */
	String[] value() default {};

	/**
	 * List of module names where this configuration should not be imported.
	 * Takes precedence over {@link #value()} and is mainly useful is {@link #value()} empty which
	 * would imply the configuration would be added to all modules (including the module providing the configuration).
	 */
	String[] exclude() default {};
}