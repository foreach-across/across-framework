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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Allows registering additional {@link ContextAnnotationAutowireCandidateResolver} classes that will be taken into account
 * to determine if a certain {@link DependencyDescriptor} is lazy or not.
 * <p>
 * This has been added to support Lazy JPA repositories (each module can register a JPA repository as lazy).
 *
 * @author Marc Vanbrabant
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LazyCompositeAutowireCandidateResolver extends ContextAnnotationAutowireCandidateResolver
{
	private static Set<ContextAnnotationAutowireCandidateResolver> ADDITIONAL_RESOLVERS = new LinkedHashSet<>();

	@Override
	@Nullable
	public Object getLazyResolutionProxyIfNecessary( DependencyDescriptor descriptor, @Nullable String beanName ) {
		for ( ContextAnnotationAutowireCandidateResolver resolver : ADDITIONAL_RESOLVERS ) {
			Object lazyResolutionProxyIfNecessary = resolver.getLazyResolutionProxyIfNecessary( descriptor, beanName );
			if ( lazyResolutionProxyIfNecessary != null ) {
				return lazyResolutionProxyIfNecessary;
			}
		}
		return ( super.isLazy( descriptor ) ? buildLazyResolutionProxy( descriptor, beanName ) : null );
	}

	public static void addAdditionalResolver( ContextAnnotationAutowireCandidateResolver resolver ) {
		ADDITIONAL_RESOLVERS.add( resolver );
	}

	public static void clearAdditionalResolvers() {
		ADDITIONAL_RESOLVERS.clear();
	}
}
