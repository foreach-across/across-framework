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

import org.springframework.core.Ordered;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that defines ordering inside the AcrossModule. The value is optional, and represents order value
 * as defined in the {@link org.springframework.core.Ordered} interface. Lower values have higher priority.
 * The default value is {@code Ordered.LOWEST_PRECEDENCE}, indicating
 * lowest priority (losing to any other specified order value).
 * <p>
 * The regular @Order annotation and Ordered interface define ordering of beans across the entire context,
 * whereas this annotation is used to second level ordering.  If you need to define the order in module at runtime,
 * use the {@link com.foreach.across.core.OrderedInModule} interface instead.
 * </p>
 *
 * @author Arne Vandamme
 * @see org.springframework.core.Ordered
 * @see com.foreach.across.core.OrderedInModule
 * @since 1.0.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface OrderInModule
{
	/**
	 * The order value. Default is {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}.
	 *
	 * @see com.foreach.across.core.OrderedInModule#getOrderInModule()
	 */
	int value() default Ordered.LOWEST_PRECEDENCE;
}
