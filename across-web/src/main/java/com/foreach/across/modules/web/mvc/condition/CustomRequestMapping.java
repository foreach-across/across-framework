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
package com.foreach.across.modules.web.mvc.condition;

import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.*;

/**
 * Custom {@link RequestMapping} that matches based on one or more {@link CustomRequestCondition} classes.
 * When building the request mapping info, beans will be created of every {@link CustomRequestCondition} class
 * added as value.
 * <p/>
 * Supports both type and method-level annotations, and can be used as a meta-annotation for creating your own
 * custom request mapping annotations.  A request must match all conditions before it will be considered.
 *
 * @see CustomRequestCondition
 * @see PrefixingRequestMappingHandlerMapping
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRequestMapping
{
	/**
	 * The {@link CustomRequestCondition} implementations that should be created and against which
	 * a request must match before the handler method will be considered.
	 */
	Class<? extends CustomRequestCondition>[] value();
}
