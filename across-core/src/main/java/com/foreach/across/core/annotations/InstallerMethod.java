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
 * Used to specify a method to executed in an @Installer annotated class.
 * Autowires the method's parameters if they are present.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InstallerMethod
{
	/**
	 * Declares whether the method arguments are required.
	 * If they are not required, parameters will not be autowired if not found.
	 * <p>Defaults to {@code true}.
	 */
	boolean required() default true;
}
