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
package com.foreach.across.test;

import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ExposedBeanDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.NewAcrossListableBeanFactory.LocalBeanListingScope;
import org.springframework.core.ResolvableType;
import support.TestContexts;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;
import static support.TestContexts.MODULE_ONE;
import static support.TestContexts.MODULE_TWO;

/**
 * Tests the Spring interface behaviour of the custom AcrossListableBeanFactory.
 * Mainly the interaction with exposed beans and bean ordering is under test.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("AcrossListableBeanFactory customizations")
@SuppressWarnings("InnerClassMayBeStatic")
class TestAcrossListableBeanFactory
{
	private AcrossListableBeanFactory parent;
	private AcrossListableBeanFactory one;
	private AcrossListableBeanFactory two;

	@Nested
	class BeanDefinitionRegistry
	{
		@Nested
		@DisplayName("BeanDefinitionRegistry.getBeanDefinition")
		class GetBeanDefinition
		{
			@Test
			@DisplayName("getBeanDefinition returns exposed definitions by default")
			void onlyRegisteredBeanDefinitionsAreReturned() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					assertThat( parent.getBeanDefinition( "numberOne" ) )
							.isInstanceOf( ExposedBeanDefinition.class )
							.isSameAs( two.getBeanDefinition( "numberOne" ) )
							.isNotSameAs( one.getBeanDefinition( "numberOne" ) );
					assertThatExceptionOfType( NoSuchBeanDefinitionException.class ).isThrownBy( () -> parent.getBeanDefinition( "internalBean" ) );

					assertThat( one.getBeanDefinition( "numberOne" ) )
							.isNotNull()
							.isNotInstanceOf( ExposedBeanDefinition.class );
					assertThat( one.getBeanDefinition( "numberTwo" ) )
							.isInstanceOf( ExposedBeanDefinition.class );
					assertThat( one.getBeanDefinition( "internalBean" ) )
							.isNotNull()
							.isNotSameAs( two.getBeanDefinition( "internalBean" ) );
				}
			}

			@Test
			@DisplayName("getBeanDefinition also returns exposed definitions in local listing mode")
			void inLocalModeExposedBeanDefinitions() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.getBeanDefinition( "numberOne" ) ).isInstanceOf( ExposedBeanDefinition.class );
					}

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeanDefinition( "numberTwo" ) ).isInstanceOf( ExposedBeanDefinition.class );
					}
				}
			}

			// todo: factory bean definition, non-factory bean
		}

		@Nested
		@DisplayName("BeanDefinitionRegistry.containsBeanDefinition")
		class ContainsBeanDefinition
		{
			@Test
			@DisplayName("containsBeanDefinition considers exposed beans")
			void exposedBeansAreConsidered() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					assertThat( parent.containsBeanDefinition( "numberOne" ) ).isTrue();
					assertThat( parent.containsBeanDefinition( "numberTwo" ) ).isTrue();
					assertThat( parent.containsBeanDefinition( "internalBean" ) ).isFalse();

					assertThat( one.containsBeanDefinition( "numberOne" ) ).isTrue();
					assertThat( one.containsBeanDefinition( "numberTwo" ) ).isTrue();
					assertThat( one.containsBeanDefinition( "internalBean" ) ).isTrue();
				}
			}

			@Test
			@DisplayName("containsBeanDefinition also considers exposed beans in local listing mode")
			void inLocalModeExposedBeanDefinitions() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.containsBeanDefinition( "numberOne" ) ).isTrue();
						assertThat( parent.containsBeanDefinition( "numberTwo" ) ).isTrue();
						assertThat( parent.containsBeanDefinition( "internalBean" ) ).isFalse();
					}

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.containsBeanDefinition( "numberOne" ) ).isTrue();
						assertThat( one.containsBeanDefinition( "numberTwo" ) ).isTrue();
						assertThat( one.containsBeanDefinition( "internalBean" ) ).isTrue();
					}
				}
			}
		}

		@Nested
		@DisplayName("BeanDefinitionRegistry.getBeanDefinitionCount")
		class GetBeanDefinitionCount
		{
			@Test
			@DisplayName("getBeanDefinitionCount is the same regardless of local listing mode")
			void localListingModeDoesNotImpact() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					int nonLocalListingCount = parent.getBeanDefinitionCount();
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.getBeanDefinitionCount() ).isSameAs( nonLocalListingCount );
					}

					nonLocalListingCount = one.getBeanDefinitionCount();
					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeanDefinitionCount() ).isSameAs( nonLocalListingCount );
					}
				}
			}
		}

		@Nested
		@DisplayName("BeanDefinitionRegistry.getBeanDefinitionNames")
		class GetBeanDefinitionNames
		{
			@Test
			@DisplayName("getBeanDefinitionNames is the same regardless of local listing mode")
			void localListingModeDoesNotImpact() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					String[] beanDefinitionNames = parent.getBeanDefinitionNames();
					assertThat( beanDefinitionNames ).contains( "numberOne", "numberTwo" ).doesNotContain( "internalBean" );
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.getBeanDefinitionNames() ).isEqualTo( beanDefinitionNames );
					}

					beanDefinitionNames = one.getBeanDefinitionNames();
					assertThat( beanDefinitionNames ).contains( "numberOne", "numberTwo", "internalBean" );
					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeanDefinitionNames() ).isEqualTo( beanDefinitionNames );
					}
				}
			}
		}

		@Nested
		@DisplayName("BeanDefinitionRegistry.isBeanNameInUse")
		class IsBeanNameInUse
		{
			@Test
			@DisplayName("isBeanNameInUse considers exposed beans - even in local listing mode")
			void localListingModeDoesNotImpact() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					assertThat( parent.isBeanNameInUse( "numberOne" ) ).isTrue();
					assertThat( parent.isBeanNameInUse( "numberTwo" ) ).isTrue();
					assertThat( parent.isBeanNameInUse( "internalBean" ) ).isFalse();

					assertThat( one.isBeanNameInUse( "numberOne" ) ).isTrue();
					assertThat( one.isBeanNameInUse( "numberTwo" ) ).isTrue();
					assertThat( one.isBeanNameInUse( "internalBean" ) ).isTrue();

					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.isBeanNameInUse( "numberOne" ) ).isTrue();
						assertThat( parent.isBeanNameInUse( "numberTwo" ) ).isTrue();
						assertThat( parent.isBeanNameInUse( "internalBean" ) ).isFalse();
					}

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.isBeanNameInUse( "numberOne" ) ).isTrue();
						assertThat( one.isBeanNameInUse( "numberTwo" ) ).isTrue();
						assertThat( one.isBeanNameInUse( "internalBean" ) ).isTrue();
					}
				}
			}
		}
	}

	@Nested
	class BeanFactory
	{
		@Nested
		@DisplayName("BeanFactory.getBean")
		class GetBean
		{
			@Test
			@DisplayName("exposed singletons return the same instance everywhere")
			void sameInstanceIsReturned() {
				try (AcrossTestContext ignore = create( TestContexts::simple )) {
					assertThat( System.identityHashCode( one.getBean( "numberOne" ) ) )
							.isEqualTo( System.identityHashCode( two.getBean( "numberOne" ) ) )
							.isEqualTo( System.identityHashCode( parent.getBean( "numberOne" ) ) );

					try (LocalBeanListingScope ignored = parent.withLocalBeanListingOnly()) {
						try (LocalBeanListingScope ignored2 = one.withLocalBeanListingOnly()) {
							assertThat( System.identityHashCode( one.getBean( "numberOne" ) ) )
									.isEqualTo( System.identityHashCode( two.getBean( "numberOne" ) ) )
									.isEqualTo( System.identityHashCode( parent.getBean( "numberOne" ) ) );
						}
					}
				}
			}
		}

		@Nested
		@DisplayName("BeanFactory.getBeanProvider")
		class GetBeanProvider
		{
			@Test
			@DisplayName("ObjectProvider returns all matching instances")
			void sameInstanceIsReturned() {
				try (AcrossTestContext ignore = create( TestContexts::simple )) {
					assertThat( parent.getBeanProvider( String.class ).iterator() ).isEmpty();
					assertThat( one.getBeanProvider( String.class ).iterator() ).containsExactly( "internalOne" );
					assertThat( two.getBeanProvider( String.class ).iterator() ).containsExactly( "internalTwo" );

					assertThat( parent.getBeanProvider( Integer.class ).iterator() ).contains( 1, 2 );
					assertThat( one.getBeanProvider( Integer.class ).iterator() ).contains( 1, 2 );
					assertThat( two.getBeanProvider( Integer.class ).iterator() ).contains( 1, 2 );
				}
			}

			@Test
			@DisplayName("ObjectProvider.orderedStream() returns beans in module order")
			void orderedStream() {
				try (AcrossTestContext ignore = create( TestContexts::simple )) {
					assertThat( parent.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 1, 2 );
					assertThat( one.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 1, 2 );
					assertThat( two.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 1, 2 );
				}

				try (AcrossTestContext ignore = create( TestContexts::simpleReversed )) {
					assertThat( parent.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 2, 1 );
					assertThat( one.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 2, 1 );
					assertThat( two.getBeanProvider( Integer.class ).orderedStream() ).containsExactly( 2, 1 );
				}
			}
		}

		@Nested
		@DisplayName("BeanFactory.containsBean")
		class ContainsBean
		{

		}

		@Nested
		@DisplayName("BeanFactory.isSingleton")
		class IsSingleton
		{

		}

		@Nested
		@DisplayName("BeanFactory.isPrototype")
		class IsPrototype
		{

		}

		@Nested
		@DisplayName("BeanFactory.isTypeMatch")
		class IsTypeMatch
		{

		}

		@Nested
		@DisplayName("BeanFactory.getType")
		class GetType
		{
			@Test
			void typeForRegularBean() {

			}

			@Test
			void typeForLazyBean() {

			}

			@Test
			void typeForFactoryBean() {

			}
		}

		@Nested
		@DisplayName("BeanFactory.getAliases")
		class GetAliases
		{
		}
	}

	@Nested
	class HierarchicalBeanFactory
	{
		@Nested
		@DisplayName("HierarchicalBeanFactory.containsLocalBean")
		class ContainsLocalBean
		{

		}
	}

	@Nested
	class ConfigurableBeanFactory
	{
		@Nested
		@DisplayName("ConfigurableBeanFactory.isFactoryBean")
		class IsFactoryBean
		{

		}

		@Nested
		@DisplayName("ConfigurableBeanFactory.getMergedBeanDefinition")
		class GetMergedBeanDefinition
		{

		}
	}

	@Nested
	class ListableBeanFactory
	{
		@Nested
		@DisplayName("ListableBeanFactory.getBeanNamesForType")
		class GetBeanNamesForType
		{
			@Test
			@DisplayName("beanNames for exposed beans")
			void beanNamesForExposedBeans() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					assertThat( parent.getBeanNamesForType( Integer.class ) )
							.isEqualTo( parent.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( parent.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
							.containsExactly( "numberOne", "numberTwo" )
							.isEqualTo( one.getBeanNamesForType( Integer.class ) )
							.isEqualTo( one.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( one.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
							.isEqualTo( two.getBeanNamesForType( Integer.class ) )
							.isEqualTo( two.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( two.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) );

					// in local mode only non-exposed bean names are returned
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( parent.getBeanNamesForType( Integer.class ) )
								.isEqualTo( parent.getBeanNamesForType( Integer.class, true, true ) )
								.isEqualTo( parent.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
								.isEmpty();
					}

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeanNamesForType( Integer.class ) )
								.isEqualTo( one.getBeanNamesForType( Integer.class, true, true ) )
								.isEqualTo( one.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
								.containsExactly( "numberOne" );
					}

					try (LocalBeanListingScope ignore = two.withLocalBeanListingOnly()) {
						assertThat( two.getBeanNamesForType( Integer.class ) )
								.isEqualTo( two.getBeanNamesForType( Integer.class, true, true ) )
								.isEqualTo( two.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
								.containsExactly( "numberTwo" );
					}
				}
			}

			@Test
			@DisplayName("beanNames are returned in module order")
			void beanNamesAreOrdered() {
				try (AcrossTestContext ignored = create( TestContexts::simpleReversed )) {
					assertThat( parent.getBeanNamesForType( Integer.class ) )
							.isEqualTo( parent.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( parent.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
							.containsExactly( "numberTwo", "numberOne" )
							.isEqualTo( one.getBeanNamesForType( Integer.class ) )
							.isEqualTo( one.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( one.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) )
							.isEqualTo( two.getBeanNamesForType( Integer.class ) )
							.isEqualTo( two.getBeanNamesForType( Integer.class, true, true ) )
							.isEqualTo( two.getBeanNamesForType( ResolvableType.forClass( Integer.class ) ) );
				}
			}

			@Test
			@DisplayName("beanNames for non-exposed beans")
			void beanNamesForNonExposedBeans() {
				try (AcrossTestContext ignored = create( TestContexts::simple )) {
					assertThat( parent.getBeanNamesForType( String.class ) )
							.isEqualTo( parent.getBeanNamesForType( String.class, true, true ) )
							.isEqualTo( parent.getBeanNamesForType( ResolvableType.forClass( String.class ) ) )
							.isEmpty();

					assertThat( one.getBeanNamesForType( String.class ) )
							.isEqualTo( one.getBeanNamesForType( String.class, true, true ) )
							.isEqualTo( one.getBeanNamesForType( ResolvableType.forClass( String.class ) ) )
							.containsExactly( "internalBean" );

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeanNamesForType( String.class ) ).containsExactly( "internalBean" );
					}

					assertThat( two.getBeanNamesForType( String.class ) )
							.isEqualTo( two.getBeanNamesForType( String.class, true, true ) )
							.isEqualTo( two.getBeanNamesForType( ResolvableType.forClass( String.class ) ) )
							.containsExactly( "internalBean" );

					try (LocalBeanListingScope ignore = two.withLocalBeanListingOnly()) {
						assertThat( two.getBeanNamesForType( String.class ) ).containsExactly( "internalBean" );
					}
				}
			}
		}

		@Nested
		@DisplayName("ListableBeanFactory.getBeansOfType")
		class GetBeansOfType
		{
			@Test
			@DisplayName("exposed beans are visible in every module")
			void simple() {
				try (AcrossTestContext ctx = create( TestContexts::simple )) {
					// strings are internal
					assertThat( beansOfTypeIncludingAncestors( parent, String.class ) ).isEmpty();
					assertThat( one.getBeansOfType( String.class ) )
							.containsExactly( entry( "internalBean", "internalOne" ) )
							.isEqualTo( beansOfTypeIncludingAncestors( one, String.class ) );
					assertThat( two.getBeansOfType( String.class ) )
							.containsExactly( entry( "internalBean", "internalTwo" ) )
							.isEqualTo( beansOfTypeIncludingAncestors( two, String.class ) );

					// numbers are exposed and returned in module order
					assertThat( beansOfTypeIncludingAncestors( parent, Integer.class ) )
							.hasSize( 2 )
							.containsExactly( entry( "numberOne", 1 ), entry( "numberTwo", 2 ) )
							.isEqualTo( one.getBeansOfType( Integer.class ) )
							.isEqualTo( beansOfTypeIncludingAncestors( one, Integer.class ) )
							.isEqualTo( two.getBeansOfType( Integer.class ) )
							.isEqualTo( beansOfTypeIncludingAncestors( two, Integer.class ) );

					// registry lookup without internals
					assertThat( ctx.getBeansOfTypeAsMap( String.class ) ).isEmpty();
					assertThat( ctx.getBeansOfTypeAsMap( Integer.class ) ).containsExactly( entry( "numberOne", 1 ), entry( "numberTwo", 2 ) );

					// registry lookup with internals
					assertThat( ctx.getBeansOfTypeAsMap( String.class, true ) )
							.containsExactly( entry( "one:internalBean", "internalOne" ), entry( "two:internalBean", "internalTwo" ) );
					assertThat( ctx.getBeansOfTypeAsMap( Integer.class, true ) )
							.containsExactly( entry( "one:numberOne", 1 ), entry( "two:numberTwo", 2 ) );

					// without exposed beans
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( beansOfTypeIncludingAncestors( parent, Integer.class ) ).isEmpty();
					}

					try (LocalBeanListingScope ignore = one.withLocalBeanListingOnly()) {
						assertThat( one.getBeansOfType( Integer.class ) ).containsExactly( entry( "numberOne", 1 ) );
					}

					try (LocalBeanListingScope ignore = two.withLocalBeanListingOnly()) {
						assertThat( two.getBeansOfType( Integer.class ) ).containsExactly( entry( "numberTwo", 2 ) );
					}

					// safety check that non-local mode is enabled again
					assertThat( beansOfTypeIncludingAncestors( parent, Integer.class ) ).hasSize( 2 );
				}
			}

			@Test
			@DisplayName("beans are returned in module order")
			void simpleWithReversedModuleOrder() {
				try (AcrossTestContext ctx = create( TestContexts::simpleReversed )) {
					// strings are internal
					assertThat( beansOfTypeIncludingAncestors( parent, String.class ) ).isEmpty();
					assertThat( one.getBeansOfType( String.class ) )
							.containsExactly( entry( "internalBean", "internalOne" ) )
							.isEqualTo( beansOfTypeIncludingAncestors( one, String.class ) );
					assertThat( two.getBeansOfType( String.class ) )
							.containsExactly( entry( "internalBean", "internalTwo" ) )
							.isEqualTo( beansOfTypeIncludingAncestors( two, String.class ) );

					// numbers are exposed and returned in module order
					assertThat( beansOfTypeIncludingAncestors( parent, Integer.class ) )
							.hasSize( 2 )
							.containsExactly( entry( "numberTwo", 2 ), entry( "numberOne", 1 ) )
							.isEqualTo( one.getBeansOfType( Integer.class ) )
							.isEqualTo( beansOfTypeIncludingAncestors( one, Integer.class ) )
							.isEqualTo( two.getBeansOfType( Integer.class ) )
							.isEqualTo( beansOfTypeIncludingAncestors( two, Integer.class ) );

					// registry lookup without internals
					assertThat( ctx.getBeansOfTypeAsMap( String.class ) ).isEmpty();
					assertThat( ctx.getBeansOfTypeAsMap( Integer.class ) ).containsExactly( entry( "numberTwo", 2 ), entry( "numberOne", 1 ) );

					// registry lookup with internals
					assertThat( ctx.getBeansOfTypeAsMap( String.class, true ) )
							.containsExactly( entry( "two:internalBean", "internalTwo" ), entry( "one:internalBean", "internalOne" ) );
					assertThat( ctx.getBeansOfTypeAsMap( Integer.class, true ) )
							.containsExactly( entry( "two:numberTwo", 2 ), entry( "one:numberOne", 1 ) );

					// without exposed beans
					try (LocalBeanListingScope ignore = parent.withLocalBeanListingOnly()) {
						assertThat( beansOfTypeIncludingAncestors( parent, Integer.class ) ).isEmpty();
					}
				}
			}
		}

		@Nested
		@DisplayName("ListableBeanFactory.getBeanNamesForAnnotation")
		class GetBeanNamesForAnnotation
		{

		}

		@Nested
		@DisplayName("ListableBeanFactory.getBeansWithAnnotation")
		class GetBeansWithAnnotation
		{

		}

		@Nested
		@DisplayName("ListableBeanFactory.findAnnotationOnBean")
		class FindAnnotationOnBean
		{

		}
	}

	@Nested
	class ConfigurableListableBeanFactory
	{
		@Nested
		@DisplayName("ConfigurableListableBeanFactory.isAutowireCandidate")
		class IsAutowireCandidate
		{
		}

		@Nested
		@DisplayName("ConfigurableListableBeanFactory.getBeanNamesIterator")
		class GetBeanNamesIterator
		{
		}
	}

	@Nested
	class AutowireCapableBeanFactory
	{
		@Nested
		@DisplayName("AutowireCapableBeanFactory.resolveDependency")
		class ResolveDependency
		{
		}
	}

	@Nested
	@DisplayName("AcrossListableBeanFactory")
	class AcrossListableBeanFactoryExtensions
	{
		@Nested
		@DisplayName("isExposedBean")
		class IsExposedBean
		{
			// bean is exposed if it is in the exposed beans
			// bean is not exposed if local bean
			// bean is not exposed if local bean even though also in exposed
		}
	}

	private AcrossTestContext create( Supplier<AcrossTestContext> supplier ) {
		AcrossTestContext testContext = supplier.get();
		parent = AcrossContextUtils.beanFactory( testContext.contextInfo() );
		one = AcrossContextUtils.beanFactory( testContext.contextInfo().getModuleInfo( MODULE_ONE ) );
		two = AcrossContextUtils.beanFactory( testContext.contextInfo().getModuleInfo( MODULE_TWO ) );
		return testContext;
	}
}
