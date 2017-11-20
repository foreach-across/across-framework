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

package com.foreach.across.core.context;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.support.AcrossOrderSpecifier;
import com.foreach.across.core.context.support.AcrossOrderUtils;
import com.foreach.across.core.registry.IncrementalRefreshableRegistry;
import com.foreach.across.core.registry.RefreshableRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extends a {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * with support for Exposed beans.  This implementation also allows the parent bean factory to be updated.
 * <p>
 * Exposed beans are fetched from the module context but are not managed by the bean factory.
 * </p>
 */
public class AcrossListableBeanFactory extends DefaultListableBeanFactory
{
	private Set<String> exposedBeanNames = new HashSet<>();

	private BeanFactory parentBeanFactory;
	private Integer moduleIndex;

	public AcrossListableBeanFactory() {
	}

	public AcrossListableBeanFactory( BeanFactory parentBeanFactory ) {
		setParentBeanFactory( parentBeanFactory );
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}

	@Override
	public void setParentBeanFactory( BeanFactory parentBeanFactory ) {
		this.parentBeanFactory = parentBeanFactory;
	}

	/**
	 * Forcibly expose the beans with the given name.  Beans exposed this way will be exposed no matter
	 * which expose filter is configured on the module.  They will still pass through the expose transformer.
	 *
	 * @param beanNames One or more bean names to expose.
	 */
	public void expose( String... beanNames ) {
		exposedBeanNames.addAll( Arrays.asList( beanNames ) );
	}

	/**
	 * @return The array of all forcibly exposed beans.
	 */
	public String[] getExposedBeanNames() {
		return exposedBeanNames.toArray( new String[exposedBeanNames.size()] );
	}

	/**
	 * Ensures ExposedBeanDefinition instances are returned as the RootBeanDefinition.
	 */
	@Override
	protected RootBeanDefinition getMergedBeanDefinition( String beanName,
	                                                      BeanDefinition bd,
	                                                      BeanDefinition containingBd ) {
		if ( bd instanceof ExposedBeanDefinition ) {
			return (ExposedBeanDefinition) bd;
		}

		return super.getMergedBeanDefinition( beanName, bd, containingBd );
	}

	/**
	 * Overridden to make public.
	 */
	@Override
	public boolean isAutowireCandidate( String beanName,
	                                    DependencyDescriptor descriptor,
	                                    AutowireCandidateResolver resolver ) throws NoSuchBeanDefinitionException {
		return super.isAutowireCandidate( beanName, descriptor, resolver );
	}

	/**
	 * An exposed bean definition does not really get created but gets fetched
	 * from the external context.
	 */
	@Override
	protected Object doCreateBean( String beanName, RootBeanDefinition mbd, Object[] args ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			List<ConstructorArgumentValues.ValueHolder> factoryArguments =
					mbd.getConstructorArgumentValues().getGenericArgumentValues();

			return ( (AcrossContextBeanRegistry) getBean( mbd.getFactoryBeanName() ) ).getBeanFromModule(
					(String) factoryArguments.get( 0 ).getValue(),
					(String) factoryArguments.get( 1 ).getValue()
			);
		}

		return super.doCreateBean( beanName, mbd, args );
	}

	@Override
	protected Class<?> getTypeForFactoryBean( String beanName, RootBeanDefinition mbd ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			List<ConstructorArgumentValues.ValueHolder> factoryArguments =
					mbd.getConstructorArgumentValues().getGenericArgumentValues();

			return ( (AcrossContextBeanRegistry) getBean( mbd.getFactoryBeanName() ) ).getBeanTypeFromModule(
					(String) factoryArguments.get( 0 ).getValue(),
					(String) factoryArguments.get( 1 ).getValue()
			);
		}

		return super.getTypeForFactoryBean( beanName, mbd );
	}

	@Override
	public Object doResolveDependency( DependencyDescriptor descriptor,
	                                   String beanName,
	                                   Set<String> autowiredBeanNames,
	                                   TypeConverter typeConverter ) throws BeansException {
		Class<?> type = descriptor.getDependencyType();

		if ( Collection.class.isAssignableFrom( type ) && type.isInterface() ) {
			Map<String, Object> attr = findRefreshableCollectionAttributes( descriptor );

			if ( attr != null ) {
				if ( !Collection.class.equals( type ) ) {
					throw new IllegalArgumentException(
							"@RefreshableCollection can only be used on the Collection interface" );
				}

				ResolvableType resolvableType = descriptor.getResolvableType();

				if ( resolvableType.hasGenerics() ) {
					resolvableType = resolvableType.getNested( 2 );
				}
				else {
					resolvableType = ResolvableType.forClass( Object.class );
				}

				RefreshableRegistry<?> registry;
				boolean includeModuleInternals = Boolean.TRUE.equals( attr.get( "includeModuleInternals" ) );
				if ( Boolean.TRUE.equals( attr.get( "incremental" ) ) ) {
					registry = new IncrementalRefreshableRegistry<>( resolvableType, includeModuleInternals );
				}
				else {
					registry = new RefreshableRegistry<>( resolvableType, includeModuleInternals );
				}

				String registryBeanName = RefreshableRegistry.class.getName() + "~" + UUID.randomUUID().toString();

				autowireBean( registry );
				initializeBean( registry, registryBeanName );
				registerSingleton( registryBeanName, registry );

				return registry;
			}

		}

		return super.doResolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );
	}

	private Map<String, Object> findRefreshableCollectionAttributes( DependencyDescriptor dependencyDescriptor ) {
		for ( Annotation candidate : dependencyDescriptor.getAnnotations() ) {
			if ( RefreshableCollection.class.isInstance( candidate ) ) {
				return AnnotationUtils.getAnnotationAttributes( candidate );
			}
		}

		if ( dependencyDescriptor.getMethodParameter() != null ) {
			Method method = dependencyDescriptor.getMethodParameter().getMethod();
			if ( method != null ) {
				Annotation annotation = AnnotationUtils.findAnnotation( method, RefreshableCollection.class );

				if ( annotation != null ) {
					return AnnotatedElementUtils.getMergedAnnotationAttributes( method, RefreshableCollection.class.getName() );
				}
			}
		}

		return null;
	}

	@Override
	public <T> Map<String, T> getBeansOfType( Class<T> type, boolean includeNonSingletons, boolean allowEagerInit ) throws BeansException {
		Map<String, T> beansOfType = super.getBeansOfType( type, includeNonSingletons, allowEagerInit );

		Map<T, String> nameForBean = new IdentityHashMap<>();
		ModuleBeanOrderComparator orderComparator = new ModuleBeanOrderComparator();
		beansOfType.forEach( ( beanName, bean ) -> {
			AcrossOrderSpecifier specifier = retrieveOrderSpecifier( beanName );
			if ( specifier != null ) {
				orderComparator.register( bean, specifier );
			}
			nameForBean.put( bean, beanName );
		} );

		return nameForBean.keySet()
		                  .stream()
		                  .sorted( orderComparator )
		                  .collect( Collectors.toMap( nameForBean::get, Function.identity(), ( v1, v2 ) -> v1, LinkedHashMap::new ) );
	}

	/**
	 * Retrieve an {@link AcrossOrderSpecifier} for a local bean or singleton.
	 * If neither is present with that name, {@code null} will be returned.
	 *
	 * @param beanName bean name
	 * @return order specifier
	 */
	public AcrossOrderSpecifier retrieveOrderSpecifier( String beanName ) {
		if ( containsBeanDefinition( beanName ) ) {
			BeanDefinition beanDefinition = getMergedLocalBeanDefinition( beanName );
			Object existing = beanDefinition.getAttribute( AcrossOrderSpecifier.class.getName() );

			if ( existing != null ) {
				return (AcrossOrderSpecifier) existing;
			}

			AcrossOrderSpecifier specifier = AcrossOrderUtils.createOrderSpecifier( beanDefinition, moduleIndex );
			beanDefinition.setAttribute( AcrossOrderSpecifier.class.getName(), specifier );
			return specifier;
		}

		if ( containsSingleton( beanName ) ) {
			return AcrossOrderSpecifier.forSources( Collections.singletonList( getSingleton( beanName ) ) ).moduleIndex( moduleIndex ).build();
		}

		return null;
	}

	/**
	 * Supports exposed bean definitions, a local - non-exposed bean definition is primary versus an exposed bean definition.
	 */
	@Override
	protected String determinePrimaryCandidate( Map<String, Object> candidates, Class<?> requiredType ) {
		String primaryBeanName = determineNonExposedCandidate( candidates );

		if ( primaryBeanName == null ) {
			primaryBeanName = super.determinePrimaryCandidate( candidates, requiredType );
		}

		return primaryBeanName;
	}

	/**
	 * Returns the single non-exposed candidate bean definition.
	 */
	private String determineNonExposedCandidate( Map<String, Object> candidates ) {
		String nonExposedCandidateBean = null;
		for ( Map.Entry<String, Object> entry : candidates.entrySet() ) {
			String candidateBeanName = entry.getKey();
			if ( containsBeanDefinition( candidateBeanName ) ) {
				if ( !( getBeanDefinition( candidateBeanName ) instanceof ExposedBeanDefinition ) ) {
					if ( nonExposedCandidateBean == null ) {
						nonExposedCandidateBean = candidateBeanName;
					}
					else {
						// more than one non-exposed - fallback to regular behaviour
						return null;
					}
				}
			}
			else {
				// not all local beans - fallback to regular behaviour
				return null;
			}
		}
		return nonExposedCandidateBean;
	}

	@Override
	public void registerBeanDefinition( String beanName,
	                                    BeanDefinition beanDefinition ) throws BeanDefinitionStoreException {
		destroySingleton( beanName );
		super.registerBeanDefinition( beanName, beanDefinition );
	}

	public void setModuleIndex( Integer moduleIndex ) {
		this.moduleIndex = moduleIndex;
	}

	public Integer getModuleIndex() {
		return moduleIndex;
	}
}
