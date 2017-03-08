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

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;

/**
 * Extension of {@link RequestCondition} that is aware of the annotated element it is for (either handler type or method).
 *
 * @param <T> request condition
 * @author Arne Vandamme
 * @see CustomRequestMapping
 * @since 2.0.0
 */
public interface CustomRequestCondition<T extends CustomRequestCondition<T>> extends RequestCondition<T>
{
	/**
	 * Set the handler type or the handler method that this condition is being created for.
	 *
	 * @param annotatedElement handler type or method
	 */
	void setAnnotatedElement( AnnotatedElement annotatedElement );

	@Override
	T combine( T other );

	@Override
	T getMatchingCondition( HttpServletRequest request );

	@Override
	int compareTo( T other, HttpServletRequest request );
}
