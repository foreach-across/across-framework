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
 * <p>
 * Annotation that checks one or more Spring expression language statements to
 * see if they all return true before deciding a bean should be created.
 * </p>
 * <p>
 * The expression can access certain objects related to the context/module being bootstrapped:
 * <ul>
 * <li><strong>currentModule</strong>: will return the AcrossModule instance currently being bootstrapped</li>
 * </ul>
 * </p>
 * <p><strong>Note:</strong> Only usable on beans & configurations, not on AcrossModule classes.</p>
 *
 * @deprecated use regular spring {@link org.springframework.boot.autoconfigure.condition.ConditionalOnExpression} instead.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(AcrossConditionCondition.class)
@Deprecated
public @interface AcrossCondition
{
	/**
	 * One or more SpEL statements.  Expressions should return {@code true} if the
	 * condition passes or {@code false} if it fails.  If multiple expressions are
	 * configured, they should all return true.
	 */
	String[] value();
}
