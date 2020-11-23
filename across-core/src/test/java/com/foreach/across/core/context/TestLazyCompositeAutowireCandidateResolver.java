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
package com.foreach.across.core.context;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TestLazyCompositeAutowireCandidateResolver
{
	@Test
	void additionalResolversCanBeRegisteredAndCleared() {
		AcrossListableBeanFactory beanFactory = new AcrossListableBeanFactory();

		Field lazyBean = ReflectionUtils.findField( TestService.class, "lazyBean" );
		Field normalBean = ReflectionUtils.findField( TestService.class, "normalBean" );
		Field jpaService = ReflectionUtils.findField( CarService.class, "carRepository" );

		DependencyDescriptor lazyBeanDescriptor = new DependencyDescriptor( lazyBean, true );
		DependencyDescriptor normalBeanDescriptor = new DependencyDescriptor( normalBean, true );
		DependencyDescriptor carDescriptor = new DependencyDescriptor( jpaService, true );

		LazyCompositeAutowireCandidateResolver candidateResolver = new LazyCompositeAutowireCandidateResolver();
		beanFactory.setAutowireCandidateResolver( candidateResolver );

		ContextAnnotationAutowireCandidateResolver mockCandidateResolver = mock( ContextAnnotationAutowireCandidateResolver.class );
		CustomLazyContextAnnotationAutowireCandidateResolver customCandidateResolver =
				new CustomLazyContextAnnotationAutowireCandidateResolver();
		customCandidateResolver.setBeanFactory( beanFactory );
		LazyCompositeAutowireCandidateResolver.addAdditionalResolver( mockCandidateResolver );
		LazyCompositeAutowireCandidateResolver.addAdditionalResolver( customCandidateResolver );

		Object lazyBeanFromSuper = candidateResolver.getLazyResolutionProxyIfNecessary( lazyBeanDescriptor, "lazyBean" );
		Object normalBeanFromSuper = candidateResolver.getLazyResolutionProxyIfNecessary( normalBeanDescriptor, "normalBean" );
		Object lazyJpaRepository = candidateResolver.getLazyResolutionProxyIfNecessary( carDescriptor, "carRepository" );

		assertTrue( AopUtils.isCglibProxy( lazyBeanFromSuper ) );
		assertTrue( AopUtils.isCglibProxy( lazyJpaRepository ) );
		assertNull( normalBeanFromSuper );

		verify( mockCandidateResolver ).getLazyResolutionProxyIfNecessary( ( eq( lazyBeanDescriptor ) ), eq( "lazyBean" ) );
		verify( mockCandidateResolver ).getLazyResolutionProxyIfNecessary( ( eq( normalBeanDescriptor ) ), eq( "normalBean" ) );
		verify( mockCandidateResolver ).getLazyResolutionProxyIfNecessary( ( eq( carDescriptor ) ), eq( "carRepository" ) );

		LazyCompositeAutowireCandidateResolver.clearAdditionalResolvers();
		verifyNoMoreInteractions( mockCandidateResolver );
		lazyBeanFromSuper = candidateResolver.getLazyResolutionProxyIfNecessary( lazyBeanDescriptor, "lazyBean" );
		normalBeanFromSuper = candidateResolver.getLazyResolutionProxyIfNecessary( normalBeanDescriptor, "normalBean" );
		lazyJpaRepository = candidateResolver.getLazyResolutionProxyIfNecessary( carDescriptor, "carRepository" );

		assertTrue( AopUtils.isCglibProxy( lazyBeanFromSuper ) );
		assertNull( normalBeanFromSuper );
		assertNull( lazyJpaRepository );
	}

	@AllArgsConstructor
	static class TestService
	{
		@Lazy
		private LazyBean lazyBean;
		private NormalBean normalBean;
	}

	static class LazyBean
	{
	}

	static class NormalBean
	{
	}

	@AllArgsConstructor
	static class CarService
	{
		private CarRepository carRepository;
	}

	static class CarRepository
	{
	}

	static class CustomLazyContextAnnotationAutowireCandidateResolver extends ContextAnnotationAutowireCandidateResolver
	{
		@Override
		protected boolean isLazy( DependencyDescriptor descriptor ) {
			return "carRepository".equals( descriptor.getField().getName() );
		}
	}
}