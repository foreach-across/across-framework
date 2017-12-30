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

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Conditional annotation that can be put on a module, @Configuration class, @Bean method or any component.
 * The actual behaviour depends on the owning element.  Using this conditional it is possible to define
 * Across module requirements.</p>
 * <p>When putting @AcrossDepends on an AcrossModule instance:
 * <ul>
 * <li>the dependencies specified will determine the bootstrap order of the module (after its dependencies)</li>
 * <li>optional dependencies are only used to optimize the bootstrap order, ensuring that any optional
 * modules are in fact bootstrapped before the current one</li>
 * <li>if any of the required dependencies are missing the AcrossContext will not be able to boot</li>
 * </ul>
 * In this case, using required and optional together is important for the best bootstrap order of the AcrossContext.
 * </p>
 * <p>When putting @AcrossDepends on a component, @Bean or @Configuration class:
 * <ul>
 * <li>if any of the <u>required</u> dependencies is <u>missing</u> the component or @Configuration will not be created</li>
 * <li>if any of the <u>optional</u> dependencies is <u>present</u> the component or @Configuration will be loaded</li>
 * </ul>
 * The latter is the implementation of the standard Spring @Conditional behavior.
 * </p>
 * <p>When putting @AcrossDepends on an installer class:
 * <ul>
 * <li>if any of the <u>required</u> dependencies is <u>missing</u> the installer will not run</li>
 * <li>if any of the <u>optional</u> dependencies is <u>present</u> the installer will execute</li>
 * </ul>
 * </p>
 * <p>
 * A module is always specified either by the name it exposes (eg. AcrossWebModule).
 * </p>
 *
 * @see org.springframework.context.annotation.Conditional
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnAcrossModule
public @interface AcrossDepends
{
	/**
	 * Set of module identifiers that are required.
	 */
	@AliasFor(annotation = ConditionalOnAcrossModule.class, attribute = "allOf")
	String[] required() default { };

	/**
	 * Set of module identifiers that are optional.
	 */
	@AliasFor(annotation = ConditionalOnAcrossModule.class, attribute = "anyOf")
	String[] optional() default { };
}
