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
package com.foreach.across.condition;

import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * {@link Condition} that checks for the presence of a {@link WebApplicationContext} and a {@link ServletContext}
 * that is not yet fully initialized.  The latter is determined by the presence of the
 * {@link AbstractAcrossServletInitializer#DYNAMIC_INITIALIZER} attribute on the {@link ServletContext}.
 *
 * @author Arne Vandamme
 * @see ConditionalOnConfigurableServletContext
 * @see ConditionalOnNotConfigurableServletContext
 * @since 2.0.0
 */
@Order(Ordered.LOWEST_PRECEDENCE - 10)
class OnConfigurableServletContext extends SpringBootCondition
{
	private static final String SERVLET_CONTEXT_BEAN = "servletContext";

	@Override
	public ConditionOutcome getMatchOutcome( ConditionContext context,
	                                         AnnotatedTypeMetadata metadata ) {
		boolean dynamicServletContextRequired
				= metadata.isAnnotated( ConditionalOnConfigurableServletContext.class.getName() );
		boolean hasServletContext = context.getBeanFactory().containsBean( SERVLET_CONTEXT_BEAN );

		if ( hasServletContext ) {
			ServletContext servletContext = context.getBeanFactory().getBean( SERVLET_CONTEXT_BEAN, ServletContext.class );

			if ( servletContext != null && isDynamicServletContext( servletContext ) ) {
				return dynamicServletContextRequired
						? ConditionOutcome.match( "found dynamic ServletContext" )
						: ConditionOutcome.noMatch( "found dynamic ServletContext" );
			}
		}

		return dynamicServletContextRequired
				? ConditionOutcome.noMatch( "no dynamic ServletContext found" )
				: ConditionOutcome.match( "no dynamic ServletContext found" );
	}

	private boolean isDynamicServletContext( ServletContext servletContext ) {
		return Boolean.TRUE.equals(
				servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER )
		);
	}
}
