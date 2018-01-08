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

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditional annotation that can be put on an Across module descriptor to define the other Across modules it depends on.
 * <ul>
 * <li>the dependencies specified will determine the bootstrap order of the module (after its dependencies)</li>
 * <li>optional dependencies are only used to optimize the bootstrap order, ensuring that any optional
 * modules are in fact bootstrapped before the current one</li>
 * <li>if any of the required dependencies are missing the AcrossContext will not be able to boot</li>
 * </ul>
 * In this case, using required and optional together is important for the best bootstrap order of the AcrossContext.
 * <p/>
 * A module is always specified either by the name it exposes (eg. AcrossWebModule).
 * <p/>
 * NOTE: Before Across 3.0.0 the same annotation was to be used as a conditional for components.
 * As of 3.0.0 this use has been deprecated, a specialized {@link ConditionalOnAcrossModule} has been added instead.
 *
 * @see ConditionalOnAcrossModule
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AcrossModuleCondition.class)
public @interface AcrossDepends
{
	/**
	 * Set of module identifiers that are required.
	 */
	String[] required() default {};

	/**
	 * Set of module identifiers that are optional.
	 */
	String[] optional() default {};
}
