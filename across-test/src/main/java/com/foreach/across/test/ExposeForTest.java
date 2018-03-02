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

package com.foreach.across.test;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation to manually expose additional components for the purpose of a test.
 * Can be used in combination with {@link org.springframework.boot.test.context.SpringBootTest} (on unit test class level),
 * as well as on the level of an {@link com.foreach.across.config.EnableAcrossContext} configuration.
 * <p/>
 * Note that {@link AcrossTestConfiguration} has an attribute {@link AcrossTestConfiguration#expose()} which is a short-hand
 * alias for adding this annotation.
 *
 * @author Arne Vandamme
 * @see AcrossTestConfiguration
 * @since 3.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Import(ExposeForTestConfiguration.class)
public @interface ExposeForTest
{
	/**
	 * Specify a collection of types, interfaces or annotations that should be exposed by all modules.
	 * Useful if you want to exposed components only for integration test purposes, especially for
	 * dynamic application modules which might not have anything exposed otherwise.
	 */
	Class<?>[] value() default {};
}
