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

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

/**
 * Annotation to use on fields that need to be autowired with a
 * {@link com.foreach.across.core.registry.RefreshableRegistry} collection type instead
 * of the collection of beans from the ApplicationContext.  An exception will be thrown if the target type
 * is not one of the interfaces implemented by {@link com.foreach.across.core.registry.RefreshableRegistry}.
 *
 * @see com.foreach.across.core.registry.RefreshableRegistry
 * @see com.foreach.across.core.registry.IncrementalRefreshableRegistry
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired(required = false)
public @interface RefreshableCollection
{
	/**
	 * @return True if the internal module beans should be picked up.
	 */
	boolean includeModuleInternals() default false;

	/**
	 * @return True if a {@link com.foreach.across.core.registry.IncrementalRefreshableRegistry} should be created.
	 */
	boolean incremental() default false;
}
