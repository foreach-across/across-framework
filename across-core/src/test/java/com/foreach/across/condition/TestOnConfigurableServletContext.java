/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.config.AcrossServletContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotationMetadata;

import javax.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestOnConfigurableServletContext
{
	private final OnConfigurableServletContext condition = new OnConfigurableServletContext();

	@Mock
	private AnnotationMetadata metadata;

	@Mock
	private ConditionContext context;

	@Mock
	private ConfigurableListableBeanFactory beanFactory;

	@BeforeEach
	public void returnBeanFactory() {
		when( context.getBeanFactory() ).thenReturn( beanFactory );
	}

	@Test
	public void dynamicNotRequiredAndNoWebApplicationContextShouldMatch() {
		match( "no dynamic ServletContext found" );
	}

	@Test
	public void dynamicRequiredButNoWebApplicationContextShouldNotMatch() {
		require();
		noMatch( "no dynamic ServletContext found" );
	}

	@Test
	public void dynamicNotRequiredAndWebApplicationContextWithInitializedServletContextShouldMatch() {
		servletContext( false );
		match( "no dynamic ServletContext found" );
	}

	@Test
	public void dynamicNotRequiredAndWebApplicationContextWithDynamicServletContextShouldNotMatch() {
		servletContext( true );
		noMatch( "found dynamic ServletContext" );
	}

	@Test
	public void dynamicRequiredAndWebApplicationContextWithInitializedServletContextShouldNotMatch() {
		require();
		servletContext( false );
		noMatch( "no dynamic ServletContext found" );
	}

	@Test
	public void dynamicRequiredAndWebApplicationContextWithDynamicServletContextShouldMatch() {
		require();
		servletContext( true );
		match( "found dynamic ServletContext" );
	}

	private void servletContext( boolean dynamic ) {
		ServletContext sc = mock( ServletContext.class );
		if ( dynamic ) {
			when( sc.getAttribute( AcrossServletContextInitializer.DYNAMIC_INITIALIZER ) ).thenReturn( true );
		}
		when( beanFactory.containsBean( "servletContext" ) ).thenReturn( true );
		when( beanFactory.getBean( "servletContext", ServletContext.class ) ).thenReturn( sc );
	}

	private void match( String message ) {
		ConditionOutcome outcome = condition.getMatchOutcome( context, metadata );
		assertTrue( outcome.isMatch() );
		assertEquals( message, outcome.getMessage() );
	}

	private void noMatch( String message ) {
		ConditionOutcome outcome = condition.getMatchOutcome( context, metadata );
		assertFalse( outcome.isMatch() );
		assertEquals( message, outcome.getMessage() );
	}

	private void require() {
		when( metadata.isAnnotated( ConditionalOnConfigurableServletContext.class.getName() ) ).thenReturn( true );
	}
}
