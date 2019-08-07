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

import com.foreach.across.core.context.info.AcrossContextInfo;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.*;

/**
 * Condition to be used on beans and configuration classes that should only execute if development mode is active.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ConditionalOnDevelopmentMode.DevelopmentModeCondition.class)
public @interface ConditionalOnDevelopmentMode
{
	class DevelopmentModeCondition implements Condition
	{
		@Override
		public boolean matches( ConditionContext context, AnnotatedTypeMetadata metadata ) {
			return context.getBeanFactory().getBean( AcrossContextInfo.class ).getContext().isDevelopmentMode();
		}
	}
}
