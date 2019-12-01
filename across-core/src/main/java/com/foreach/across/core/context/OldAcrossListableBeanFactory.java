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

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.support.AcrossContextEagerRefreshHandler;
import com.foreach.across.core.context.support.AcrossLifecycleProcessor;
import com.foreach.across.core.context.support.AcrossOrderSpecifier;
import com.foreach.across.core.context.support.AcrossOrderUtils;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.beans.factory.BeanFactoryUtils.isFactoryDereference;
import static org.springframework.context.support.AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME;

/**
 * Extends a {@link DefaultListableBeanFactory}
 * with support for Exposed beans.  This implementation also allows the parent bean factory to be updated.
 * <p>
 * Exposed beans are fetched from the module context but are not managed by the bean factory.
 * </p>
 */
@Deprecated
@SuppressWarnings("serial")
public class OldAcrossListableBeanFactory extends DefaultListableBeanFactory
{
	private static final ThreadLocal<Map<Object, Boolean>> localBeansOperationMode = new ThreadLocal<>();

	private final Set<String> exposedBeanNames = new HashSet<>();
	private final Map<String, AcrossContextBeanRegistry> acrossBeanRegistriesCache = new HashMap<>( 1 );
	private final ConcurrentMap<String, Boolean> exposedBeansCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Optional<AcrossOrderSpecifier>> orderSpecifierCache = new ConcurrentHashMap<>();
	private transient final AcrossOrderComparator acrossOrderComparator = new AcrossOrderComparator();
	private BeanFactory parentBeanFactory;
	private Integer moduleIndex;
	private String moduleName;

	public OldAcrossListableBeanFactory() {
		AcrossLifecycleProcessor lifecycleProcessor = new AcrossLifecycleProcessor();
		lifecycleProcessor.setBeanFactory( this );
		registerSingleton( LIFECYCLE_PROCESSOR_BEAN_NAME, lifecycleProcessor );
	}

	public OldAcrossListableBeanFactory( BeanFactory parentBeanFactory ) {
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

	public void setModuleIndex( Integer moduleIndex ) {
		this.moduleIndex = moduleIndex;
	}

	public Integer getModuleIndex() {
		return moduleIndex;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName( String moduleName ) {
		this.moduleName = moduleName;
	}

	/**
	 * Forcibly expose the beans with the given name.  Beans exposed this way will be exposed no matter
	 * which expose filter is configured on the module.  They will still pass through the expose transformer.
	 *
	 * @param beanNames One or more bean names to expose.
	 */
	@Deprecated
	public void expose( String... beanNames ) {
		exposedBeanNames.addAll( Arrays.asList( beanNames ) );
	}

	/**
	 * @return The array of all forcibly exposed beans.
	 */
	@Deprecated
	public String[] getExposedBeanNames() {
		return exposedBeanNames.toArray( new String[0] );
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

	@Override
	protected boolean isAutowireCandidate( String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor, AutowireCandidateResolver resolver ) {
		if ( mbd instanceof ExposedBeanDefinition && moduleName != null ) {
			if ( moduleName.equals( ( (ExposedBeanDefinition) mbd ).getModuleName() ) ) {
				// exposed bean from the current module
				return false;
			}
		}
		return super.isAutowireCandidate( beanName, mbd, descriptor, resolver );
	}

	@Override
	protected String determineAutowireCandidate( Map<String, Object> candidates, DependencyDescriptor descriptor ) {
		return super.determineAutowireCandidate( candidates, descriptor );
	}

	/**
	 * Ensures ExposedBeanDefinition instances are returned as the RootBeanDefinition.
	 */
	@Override
	protected RootBeanDefinition getMergedBeanDefinition( String beanName, BeanDefinition bd, BeanDefinition containingBd ) {
		if ( bd instanceof ExposedBeanDefinition ) {
			return (ExposedBeanDefinition) bd;
		}

		return super.getMergedBeanDefinition( beanName, bd, containingBd );
	}

	@Override
	public Class<?> getType( String name ) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName( name );

		if ( beanName != null && containsBeanDefinition( beanName ) ) {
			BeanDefinition bd = getBeanDefinition( beanName );

			if ( bd instanceof ExposedBeanDefinition ) {
				List<ConstructorArgumentValues.ValueHolder> factoryArguments = bd.getConstructorArgumentValues().getGenericArgumentValues();

				String moduleBeanName = (String) factoryArguments.get( 1 ).getValue();
				return acrossContextBeanRegistry( bd.getFactoryBeanName() ).getBeanTypeFromModule(
						(String) factoryArguments.get( 0 ).getValue(),
						isFactoryDereference( name ) ? FACTORY_BEAN_PREFIX + moduleBeanName : moduleBeanName
				);
			}
		}

		return super.getType( name );
	}

	@Override
	protected Class<?> getTypeForFactoryBean( String beanName, RootBeanDefinition mbd ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			List<ConstructorArgumentValues.ValueHolder> factoryArguments =
					mbd.getConstructorArgumentValues().getGenericArgumentValues();

			return acrossContextBeanRegistry( mbd.getFactoryBeanName() ).getBeanTypeFromModule(
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

		Map<String, Object> refreshableCollection = findRefreshableCollectionAttributes( descriptor );

		if ( refreshableCollection != null ) {
			Object target = super.doResolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );

			target = convertToRefreshableType( target, type );

			if ( target instanceof Collection || target instanceof Map ) {
				AcrossContextEagerRefreshHandler refreshHandler = getBean( AcrossContextEagerRefreshHandler.class );

				boolean includeModuleInternals = Boolean.TRUE.equals( refreshableCollection.get( "includeModuleInternals" ) );
				boolean afterEachModule = Boolean.TRUE.equals( refreshableCollection.get( "incremental" ) );

				if ( !includeModuleInternals ) {
					refreshHandler.addRefreshTarget(
							target,
							t -> {
								Object newTarget = super.doResolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );

								if ( t instanceof Map ) {
									Map originalMap = (Map) t;
									originalMap.clear();
									if ( newTarget != null ) {
										originalMap.putAll( (Map) newTarget );
									}
								}
								else if ( t instanceof Collection ) {
									Collection originalCollection = (Collection) t;
									originalCollection.clear();
									if ( newTarget != null ) {
										originalCollection.addAll( (Collection) newTarget );
									}
								}
							},
							afterEachModule
					);
				}
				else {
					AcrossContextBeanRegistry contextBeanRegistry = getBean( AcrossContextBeanRegistry.class );
					ResolvableType resolvableType = descriptor.getResolvableType();

					if ( resolvableType.hasGenerics() ) {
						resolvableType = resolvableType.getNested( 2 );
					}
					else {
						resolvableType = ResolvableType.forClass( Object.class );
					}

					final ResolvableType lookupType = resolvableType;

					@NonNull Consumer<Object> refreshFunction = t -> {
						Map newTarget = contextBeanRegistry.getBeansOfTypeAsMap( lookupType, true );

						if ( t instanceof Map ) {
							Map originalMap = (Map) t;
							originalMap.clear();
							if ( newTarget != null ) {
								originalMap.putAll( newTarget );
							}
						}
						else if ( t instanceof Collection ) {
							Collection originalCollection = (Collection) t;
							originalCollection.clear();
							if ( newTarget != null ) {
								originalCollection.addAll( newTarget.values() );
							}
						}
					};
					refreshHandler.addRefreshTarget(
							target,
							refreshFunction,
							afterEachModule
					);
					refreshFunction.accept( target );
				}

				return target;
			}
			else {
				throw new IllegalArgumentException( "@RefreshableCollection can only be used on the Collection or Map interface" );
			}

			// if internal? -> contextBeanRegistry() else -> super.doResolveDependency()
			// merge
		}

		//
		// if ( Collection.class.isAssignableFrom( type ) && type.isInterface() ) {
		// //	Map<String, Object> attr = findRefreshableCollectionAttributes( descriptor );
		//
		// 	if ( attr != null ) {
		// 		if ( !Collection.class.equals( type ) ) {
		// 			throw new IllegalArgumentException(
		// 					"@RefreshableCollection can only be used on the Collection interface" );
		// 		}
		//
		// 		ResolvableType resolvableType = descriptor.getResolvableType();
		//
		// 		if ( resolvableType.hasGenerics() ) {
		// 			resolvableType = resolvableType.getNested( 2 );
		// 		}
		// 		else {
		// 			resolvableType = ResolvableType.forClass( Object.class );
		// 		}
		//
		// 		RefreshableRegistry<?> registry;
		// 		boolean includeModuleInternals = Boolean.TRUE.equals( attr.get( "includeModuleInternals" ) );
		// 		if ( Boolean.TRUE.equals( attr.get( "incremental" ) ) ) {
		// 			registry = new IncrementalRefreshableRegistry<>( resolvableType, includeModuleInternals );
		// 		}
		// 		else {
		// 			registry = new RefreshableRegistry<>( resolvableType, includeModuleInternals );
		// 		}
		//
		// 		String registryBeanName = RefreshableRegistry.class.getName() + "~" + UUID.randomUUID().toString();
		//
		// 		autowireBean( registry );
		// 		initializeBean( registry, registryBeanName );
		// 		registerSingleton( registryBeanName, registry );
		//
		// 		return registry;
		// 	}
		//
		// }

		return super.doResolveDependency( descriptor, beanName, autowiredBeanNames, typeConverter );
	}

	private Object convertToRefreshableType( Object original, Class<?> type ) {
		if ( List.class.equals( type ) || Collection.class.equals( type ) ) {
			return new ArrayList<>( original == null ? Collections.emptyList() : (Collection) original );
		}
		if ( Set.class.equals( type ) ) {
			return new LinkedHashSet<>( original == null ? Collections.emptyList() : (Collection) original );
		}
		if ( Map.class.equals( type ) ) {
			return new LinkedHashMap<>( original == null ? Collections.emptyMap() : (Map) original );
		}

		return original;
	}

	/**
	 * An exposed bean definition does not really get created but gets fetched from the external context.
	 */
	@Override
	protected Object doCreateBean( String beanName, RootBeanDefinition mbd, Object[] args ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			List<ConstructorArgumentValues.ValueHolder> factoryArguments =
					mbd.getConstructorArgumentValues().getGenericArgumentValues();

			return acrossContextBeanRegistry( mbd.getFactoryBeanName() ).getBeanFromModule(
					(String) factoryArguments.get( 0 ).getValue(),
					(String) factoryArguments.get( 1 ).getValue()
			);
		}

		return super.doCreateBean( beanName, mbd, args );
	}

	@Override
	protected <T> T doGetBean( String name, Class<T> requiredType, Object[] args, boolean typeCheckOnly ) throws BeansException {
		String beanName = BeanFactoryUtils.transformedBeanName( name );
		if ( isExposedBean( beanName ) ) {
			ExposedBeanDefinition mbd = (ExposedBeanDefinition) getBeanDefinition( beanName );
			AcrossContextInfo contextInfo = acrossContextBeanRegistry( mbd.getFactoryBeanName() ).getContextInfo();
			OldAcrossListableBeanFactory moduleBeanFactory =
					(OldAcrossListableBeanFactory) ( mbd.getModuleName() != null
							? contextInfo.getModuleInfo( mbd.getModuleName() ).getApplicationContext().getAutowireCapableBeanFactory()
							: contextInfo.getApplicationContext().getAutowireCapableBeanFactory() );

			String originalBeanName = isFactoryDereference( name ) ? FACTORY_BEAN_PREFIX + mbd.getOriginalBeanName() : mbd.getOriginalBeanName();
			return moduleBeanFactory.doGetBean( originalBeanName, requiredType, args, typeCheckOnly );
		}

		return super.doGetBean( name, requiredType, args, typeCheckOnly );
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
		AcrossOrderSpecifierComparator orderComparator = new AcrossOrderSpecifierComparator();
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
		                  .collect(
				                  Collectors.toMap( nameForBean::get, Function.identity(), ( v1, v2 ) -> v1, LinkedHashMap::new ) );

	}

	@Override
	public String[] getBeanNamesForType( ResolvableType type ) {
		return filterEposedBeanNames( super.getBeanNamesForType( type ) );
	}

	@Override
	public String[] getBeanNamesForType( Class<?> type ) {
		return filterEposedBeanNames( super.getBeanNamesForType( type ) );
	}

	@Override
	public String[] getBeanNamesForType( Class<?> type, boolean includeNonSingletons, boolean allowEagerInit ) {
		return filterEposedBeanNames( super.getBeanNamesForType( type, includeNonSingletons, allowEagerInit ) );
	}

	private String[] filterEposedBeanNames( String[] beanNames ) {
		if ( isInLocalBeansOperationMode() ) {
			return Stream.of( beanNames ).filter( name -> !isExposedBean( name ) ).toArray( String[]::new );
		}

		return beanNames;
	}

	/**
	 * Retrieve an {@link AcrossOrderSpecifier} for a local bean or singleton.
	 * If neither is present with that name, {@code null} will be returned.
	 *
	 * @param beanName bean name
	 * @return order specifier
	 */
	public AcrossOrderSpecifier retrieveOrderSpecifier( String beanName ) {
		return orderSpecifierCache.computeIfAbsent( beanName, this::buildOrderSpecifierForBean ).orElse( null );
	}

	private Optional<AcrossOrderSpecifier> buildOrderSpecifierForBean( String beanName ) {
		if ( containsBeanDefinition( beanName ) ) {
			BeanDefinition beanDefinition = getMergedLocalBeanDefinition( beanName );
			Object existing = beanDefinition.getAttribute( AcrossOrderSpecifier.class.getName() );

			if ( existing != null ) {
				return Optional.of( (AcrossOrderSpecifier) existing );
			}

			AcrossOrderSpecifier specifier = AcrossOrderUtils.createOrderSpecifier( beanDefinition, moduleIndex );
			beanDefinition.setAttribute( AcrossOrderSpecifier.class.getName(), specifier );
			return Optional.of( specifier );
		}

		if ( containsSingleton( beanName ) ) {
			return Optional.of(
					AcrossOrderSpecifier.forSources( Collections.singletonList( getSingleton( beanName ) ) ).moduleIndex( moduleIndex ).build()
			);
		}

		return Optional.empty();
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
	public void registerBeanDefinition( String beanName, BeanDefinition beanDefinition ) throws BeanDefinitionStoreException {
		destroySingleton( beanName );
		super.registerBeanDefinition( beanName, beanDefinition );
	}

	@Override
	public Comparator<Object> getDependencyComparator() {
		return acrossOrderComparator;
	}

	/**
	 * Check if a bean with a given name is an exposed bean.
	 */
	public boolean isExposedBean( String beanName ) {
		return exposedBeansCache.computeIfAbsent( beanName, this::representsExposedBean );
	}

	private boolean representsExposedBean( String beanName ) {
		return containsBeanDefinition( beanName ) && getBeanDefinition( beanName ) instanceof ExposedBeanDefinition;
	}

	@Override
	public boolean containsSingleton( String beanName ) {
		// exposed singletons are considered to be part of the bean factory
		if ( containsBeanDefinition( beanName ) ) {
			BeanDefinition bd = getBeanDefinition( beanName );
			if ( bd instanceof ExposedBeanDefinition && bd.isSingleton() ) {
				return true;
			}
		}

		return super.containsSingleton( beanName );
	}

	@Override
	public boolean isFactoryBean( String name ) throws NoSuchBeanDefinitionException {
		// custom implementation that skips custom "containsSingleton"
		String beanName = transformedBeanName( name );
		Object beanInstance = getSingleton( beanName, false );
		if ( beanInstance != null ) {
			return ( beanInstance instanceof FactoryBean );
		}
		else if ( super.containsSingleton( beanName ) ) {
			// null instance registered
			return false;
		}
		// No singleton instance found -> check bean definition.
		if ( !containsBeanDefinition( beanName ) && getParentBeanFactory() instanceof ConfigurableBeanFactory ) {
			// No bean definition found in this factory -> delegate to parent.
			return ( (ConfigurableBeanFactory) getParentBeanFactory() ).isFactoryBean( name );
		}
		return isFactoryBean( beanName, getMergedLocalBeanDefinition( beanName ) );
	}

	@Override
	protected boolean isFactoryBean( String beanName, RootBeanDefinition mbd ) {
		BeanDefinition bd = mbd;

		if ( isFactoryDereference( beanName ) ) {
			String originalBeanName = BeanFactoryUtils.transformedBeanName( beanName );
			if ( isExposedBean( originalBeanName ) ) {
				bd = getBeanDefinition( originalBeanName );
			}
		}

		if ( bd instanceof ExposedBeanDefinition ) {
			ExposedBeanDefinition ebd = (ExposedBeanDefinition) bd;
			AcrossContextInfo contextInfo = acrossContextBeanRegistry( ebd.getFactoryBeanName() ).getContextInfo();
			try {
				OldAcrossListableBeanFactory moduleBeanFactory =
						(OldAcrossListableBeanFactory) ( ebd.getModuleName() != null
								? contextInfo.getModuleInfo( ebd.getModuleName() ).getApplicationContext().getAutowireCapableBeanFactory()
								: contextInfo.getApplicationContext().getAutowireCapableBeanFactory() );

				return moduleBeanFactory.isFactoryBean( isFactoryDereference( beanName )
						                                        ? BeanFactory.FACTORY_BEAN_PREFIX + ebd.getOriginalBeanName()
						                                        : ebd.getOriginalBeanName() );
			}
			catch ( IllegalStateException ise ) {
				// Most likely already shutdown - exposed beans are no longer correctly available
				return false;
			}
		}

		return super.isFactoryBean( beanName, mbd );
	}

	@Override
	public boolean isTypeMatch( String name, ResolvableType typeToMatch ) throws NoSuchBeanDefinitionException {
		if ( isFactoryDereference( name ) ) {
			String beanName = BeanFactoryUtils.transformedBeanName( name );
			if ( isExposedBean( beanName ) ) {
				ExposedBeanDefinition mbd = (ExposedBeanDefinition) getBeanDefinition( beanName );
				AcrossContextInfo contextInfo = acrossContextBeanRegistry( mbd.getFactoryBeanName() ).getContextInfo();
				OldAcrossListableBeanFactory moduleBeanFactory =
						(OldAcrossListableBeanFactory) ( mbd.getModuleName() != null
								? contextInfo.getModuleInfo( mbd.getModuleName() ).getApplicationContext().getAutowireCapableBeanFactory()
								: contextInfo.getApplicationContext().getAutowireCapableBeanFactory() );
				RootBeanDefinition originalBd = moduleBeanFactory.getMergedLocalBeanDefinition( mbd.getOriginalBeanName() );

				return moduleBeanFactory.isFactoryBean( mbd.getOriginalBeanName(), originalBd )
						&& moduleBeanFactory.isTypeMatch( FACTORY_BEAN_PREFIX + mbd.getOriginalBeanName(), typeToMatch );
			}
		}

		return super.isTypeMatch( name, typeToMatch );
	}

	/**
	 * Puts this bean factory in local bean listing mode. This will adjust the {@link ListableBeanFactory} methods to
	 * only consider non-exposed beans. Because the exposed bean definitions are still present, other bean factory
	 * methods will still consider exposed bean definitions.
	 * <p/>
	 * Mainly for internal use in the framework.
	 * <p/>
	 * <strong>WARNING:</strong> use this only in an appropriate try-with-resources construction, ensure that you
	 * release the scope by calling {@link LocalBeanListingScope#close()}.
	 *
	 * @return closeable
	 * @see com.foreach.across.core.events.NonExposedEventListenerMethodProcessor
	 */
	public LocalBeanListingScope withLocalBeanListingOnly() {
		return new LocalBeanListingScope();
	}

	@Override
	protected void resetBeanDefinition( String beanName ) {
		super.resetBeanDefinition( beanName );
	}

	@Override
	protected boolean requiresDestruction( Object bean, RootBeanDefinition mbd ) {
		if ( isExposedBean( mbd ) ) {
			// exposed beans should be destroyed in their original bean factory
			return false;
		}
		return super.requiresDestruction( bean, mbd );
	}

	private boolean isInLocalBeansOperationMode() {
		Map<Object, Boolean> beanFactoryMap = localBeansOperationMode.get();
		return beanFactoryMap != null && Boolean.TRUE.equals( beanFactoryMap.get( this ) );
	}

	private boolean isExposedBean( BeanDefinition beanDefinition ) {
		return beanDefinition instanceof ExposedBeanDefinition;
	}

	private AcrossContextBeanRegistry acrossContextBeanRegistry( String beanName ) {
		return acrossBeanRegistriesCache.computeIfAbsent( beanName, bn -> (AcrossContextBeanRegistry) getBean( bn ) );
	}

	/**
	 * Custom {@link OrderComparator} in order to replace the default ordering logic with AcrossSpecifier based.
	 * Uses reflection to retrieve private values from the source provider as otherwise custom implementation
	 * on the entire autowiring would be required.
	 */
	private class AcrossOrderComparator extends OrderComparator
	{
		private Field instancesField;

		@SneakyThrows
		@SuppressWarnings("unchecked")
		@Override
		public Comparator<Object> withSourceProvider( OrderSourceProvider sourceProvider ) {
			if ( instancesField == null ) {
				instancesField = ReflectionUtils.findField( sourceProvider.getClass(), "instancesToBeanNames" );
				instancesField.setAccessible( true );
			}

			Map<Object, String> instancesToBeanNames = (Map<Object, String>) instancesField.get( sourceProvider );

			AcrossOrderSpecifierComparator comparator = new AcrossOrderSpecifierComparator();
			instancesToBeanNames.forEach( ( bean, beanName ) -> comparator.register( bean, retrieveOrderSpecifier( beanName ) ) );

			return comparator;
		}

	}

	/**
	 * @see #withLocalBeanListingOnly()
	 */
	public class LocalBeanListingScope implements AutoCloseable
	{
		private LocalBeanListingScope() {
			Map<Object, Boolean> beanFactoryMap = localBeansOperationMode.get();
			if ( beanFactoryMap == null ) {
				beanFactoryMap = new IdentityHashMap<>();
				localBeansOperationMode.set( beanFactoryMap );
			}
			beanFactoryMap.put( OldAcrossListableBeanFactory.this, true );

		}

		@Override
		public void close() {
			Map<Object, Boolean> beanFactoryMap = localBeansOperationMode.get();
			if ( beanFactoryMap != null ) {
				beanFactoryMap.remove( OldAcrossListableBeanFactory.this );
				if ( beanFactoryMap.isEmpty() ) {
					localBeansOperationMode.remove();
				}
			}
		}
	}
}

