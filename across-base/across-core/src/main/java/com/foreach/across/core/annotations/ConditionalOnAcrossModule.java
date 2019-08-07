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
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Conditional annotation that can be put on an @Configuration class, @Bean method or any component.
 * Using this conditional it is possible to define Across module requirements.</p>
 * <p>When putting @ConditionalOnAcrossModule on a component, @Bean or @Configuration class:
 * <ul>
 * <li>if any of the <u>allOf</u> dependencies is <u>missing</u> the component or @Configuration will not be created</li>
 * <li>if any of the <u>noneOf</u> dependencies is <u>present</u> the component or @Configuration will not be created</li>
 * <li>if any of the <u>anyOf</u> dependencies is <u>present</u> the component or @Configuration will be loaded</li>
 * </ul>
 * </p>
 * <p>When putting @ConditionalOnAcrossModule on an installer class:
 * <ul>
 * <li>if any of the <u>anyOf</u> dependencies is <u>missing</u> the installer will not run</li>
 * <li>if any of the <u>noneOf</u> dependencies is <u>present</u> the installer will not run</li>
 * <li>if any of the <u>anyOf</u> dependencies is <u>present</u> the installer will execute</li>
 * </ul>
 * </p>
 * <p>
 * A module is always specified either by the name it exposes (eg. AcrossWebModule).
 * </p>
 *
 * @author Steven Gentens
 * @see org.springframework.context.annotation.Conditional
 * @since 3.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AcrossModuleCondition.class)
public @interface ConditionalOnAcrossModule
{
	/**
	 * Set of module identifiers that should be present.
	 * Alias for {@link #allOf()}.
	 */
	@AliasFor("allOf")
	String[] value() default {};

	/**
	 * Set of module identifiers that should be present.
	 */
	@AliasFor("value")
	String[] allOf() default {};

	/**
	 * Set of module identifiers of which at least one should be present.
	 */
	String[] anyOf() default {};

	/**
	 * Set of module identifiers that are not allowed to be present.
	 */
	String[] noneOf() default {};
}
