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
package com.foreach.across.test.refresh;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.support.AcrossTestBuilders;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
class TestRefreshableCollection
{
	@Test
	void collectionIsAlwaysCreatedEvenIfNoResults() {

	}

	@Test
	void defaultRefreshableCollectionOnlyReturnsExposedBeans() {
		try (AcrossTestContext ctx = AcrossTestBuilders.standard( false )
		                                               .modules(
				                                               new EmptyAcrossModule( "one", NonInternalsNonIncremental.class, ExposedNumber.class ),
				                                               new EmptyAcrossModule( "two", NonExposedNumber.class ),
				                                               new EmptyAcrossModule( "three", ExposedNumber.class )
		                                               )
		                                               .configurer(
				                                               c -> c.addApplicationContextConfigurer( new AnnotatedClassConfigurer( ParentNumber.class ),
				                                                                                       ConfigurerScope.CONTEXT_ONLY ) )
		                                               .build()) {
			CollectionsHolder holder = ctx.getBeanOfType( CollectionsHolder.class );
			assertThat( holder.numbersCollection )
					//.containsExactlyElementsOf( holder.numbersList )
					//.containsExactlyElementsOf( holder.numbersSet )
					//.containsExactlyElementsOf( holder.numbersMaps.values() )
					.containsExactly( new AtomicInteger( -1 ), new AtomicInteger( 2 ), new AtomicInteger( 4 ) );
		}
	}

	@Test
	void moduleInternalsNonIncremental() {

	}

	@Test
	void incrementalWithoutInternals() {

	}

	@Test
	void moduleInternalsWithIncremental() {

	}

	@Configuration
	static class NonInternalsNonIncremental
	{
		@Bean
		@Exposed
		CollectionsHolder collectionsHolder( @RefreshableCollection Collection<AtomicInteger> numbersCollection,
		                                     @RefreshableCollection List<AtomicInteger> numbersList,
		                                     @RefreshableCollection Set<AtomicInteger> numbersSet,
		                                     @RefreshableCollection Map<String, AtomicInteger> numbersMap ) {
			return new CollectionsHolder( numbersCollection, numbersList, numbersSet, numbersMap );
		}
	}

	@RequiredArgsConstructor
	static class CollectionsHolder
	{
		private final Collection<AtomicInteger> numbersCollection;
		private final List<AtomicInteger> numbersList;
		private final Set<AtomicInteger> numbersSet;
		private final Map<String, AtomicInteger> numbersMaps;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	static class ExposedNumber
	{
		@Bean
		@Exposed
		AtomicInteger number( Optional<AcrossModuleInfo> moduleInfo ) {
			return new AtomicInteger( moduleInfo.map( AcrossModuleInfo::getIndex ).orElse( -1 ) );
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	static class NonExposedNumber
	{
		@Bean
		AtomicInteger number( Optional<AcrossModuleInfo> moduleInfo ) {
			return new AtomicInteger( moduleInfo.map( AcrossModuleInfo::getIndex ).orElse( -1 ) );
		}
	}

	static class ParentNumber
	{
		@Bean
		AtomicInteger number() {
			return new AtomicInteger( -1 );
		}
	}
}
