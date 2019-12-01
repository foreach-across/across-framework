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
package org.springframework.beans.factory.support;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.support.AcrossOrderSpecifier;
import com.foreach.across.core.context.support.AcrossOrderUtils;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

import javax.inject.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.springframework.beans.factory.BeanFactoryUtils.isFactoryDereference;

/**
 * Custom bean factory implementation which replaces {@link DefaultListableBeanFactory}
 * in an Across application. Copied from the {@link DefaultListableBeanFactory} but using
 * the same package to avoid having to copy the package protected classes.
 *
 * @author Arne Vandamme
 * @see DefaultListableBeanFactory
 * @since 5.0.0
 */
@SuppressWarnings("serial")
public abstract class NewAcrossListableBeanFactory extends DefaultListableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable
{
	/**
	 * Holds the state that determines if this bean factory is operating in local listing mode.
	 * When in local listing mode it will not include the exposed beans when calling any of the
	 * get beans of type related methods.
	 */
	private static final ThreadLocal<Map<Object, Boolean>> localBeansOperationMode = new ThreadLocal<>();

	@Nullable
	private static Class<?> javaxInjectProviderClass;

	static {
		try {
			javaxInjectProviderClass =
					ClassUtils.forName( "javax.inject.Provider", NewAcrossListableBeanFactory.class.getClassLoader() );
		}
		catch ( ClassNotFoundException ex ) {
			// JSR-330 API not available - Provider interface simply not supported then.
			javaxInjectProviderClass = null;
		}
	}

	/**
	 * Map from serialized id to factory instance.
	 */
	private static final Map<String, Reference<NewAcrossListableBeanFactory>> serializableFactories = new ConcurrentHashMap<>( 8 );

	/**
	 * Optional id for this factory, for serialization purposes.
	 */
	@Nullable
	private String serializationId;

	/**
	 * Whether to allow re-registration of a different definition with the same name.
	 */
	private boolean allowBeanDefinitionOverriding = true;

	/**
	 * Whether to allow eager class loading even for lazy-init beans.
	 */
	private boolean allowEagerClassLoading = true;

	/**
	 * Optional OrderComparator for dependency Lists and arrays.
	 */
	@Nullable
	private Comparator<Object> dependencyComparator;

	/**
	 * Resolver to use for checking if a bean definition is an autowire candidate.
	 */
	private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();

	/**
	 * Map from dependency type to corresponding autowired value.
	 */
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>( 16 );

	/**
	 * Map of bean definition objects, keyed by bean name.
	 */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>( 256 );

	/**
	 * Map of singleton and non-singleton bean names, keyed by dependency type.
	 */
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>( 64 );

	/**
	 * Map of singleton-only bean names, keyed by dependency type.
	 */
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>( 64 );

	/**
	 * List of bean definition names, in registration order.
	 */
	private volatile List<String> beanDefinitionNames = new ArrayList<>( 256 );

	/**
	 * List of names of manually registered singletons, in registration order.
	 */
	private volatile Set<String> manualSingletonNames = new LinkedHashSet<>( 16 );

	/**
	 * Cached array of bean definition names in case of frozen configuration.
	 */
	@Nullable
	private volatile String[] frozenBeanDefinitionNames;

	/**
	 * Whether bean definition metadata may be cached for all beans.
	 */
	private volatile boolean configurationFrozen = false;

	/**
	 * Optional module index if this beanfactory actually represents an Across module.
	 */
	@Setter
	@Nullable
	private Integer moduleIndex;

	/**
	 * Holds the parent bean factory, because we must allow changing the parent after the beanfactory
	 * has been created, we shadown the field from the {@link AbstractBeanFactory}.
	 */
	@Nullable
	private BeanFactory parentBeanFactory;

	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	protected NewAcrossListableBeanFactory() {
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 *
	 * @param parentBeanFactory the parent BeanFactory
	 */
	protected NewAcrossListableBeanFactory( @Nullable BeanFactory parentBeanFactory ) {
		super( parentBeanFactory );
	}

	/**
	 * Specify an id for serialization purposes, allowing this BeanFactory to be
	 * deserialized from this id back into the BeanFactory object, if needed.
	 */
	public void setSerializationId( @Nullable String serializationId ) {
		if ( serializationId != null ) {
			serializableFactories.put( serializationId, new WeakReference<>( this ) );
		}
		else if ( this.serializationId != null ) {
			serializableFactories.remove( this.serializationId );
		}
		this.serializationId = serializationId;
	}

	/**
	 * Return an id for serialization purposes, if specified, allowing this BeanFactory
	 * to be deserialized from this id back into the BeanFactory object, if needed.
	 *
	 * @since 4.1.2
	 */
	@Nullable
	public String getSerializationId() {
		return this.serializationId;
	}

	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. This also applies to overriding aliases.
	 * <p>Default is "true".
	 *
	 * @see #registerBeanDefinition
	 */
	public void setAllowBeanDefinitionOverriding( boolean allowBeanDefinitionOverriding ) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Return whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 *
	 * @since 4.1.2
	 */
	public boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	/**
	 * Set whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 * <p>Default is "true". Turn this flag off to suppress class loading
	 * for lazy-init beans unless such a bean is explicitly requested.
	 * In particular, by-type lookups will then simply ignore bean definitions
	 * without resolved class name, instead of loading the bean classes on
	 * demand just to perform a type check.
	 *
	 * @see AbstractBeanDefinition#setLazyInit
	 */
	public void setAllowEagerClassLoading( boolean allowEagerClassLoading ) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	/**
	 * Return whether the factory is allowed to eagerly load bean classes
	 * even for bean definitions that are marked as "lazy-init".
	 *
	 * @since 4.1.2
	 */
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	/**
	 * Set a {@link java.util.Comparator} for dependency Lists and arrays.
	 *
	 * @see org.springframework.core.OrderComparator
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 * @since 4.0
	 */
	public void setDependencyComparator( @Nullable Comparator<Object> dependencyComparator ) {
		this.dependencyComparator = dependencyComparator;
	}

	/**
	 * Return the dependency comparator for this BeanFactory (may be {@code null}.
	 *
	 * @since 4.0
	 */
	@Nullable
	public Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	/**
	 * Set a custom autowire candidate resolver for this BeanFactory to use
	 * when deciding whether a bean definition should be considered as a
	 * candidate for autowiring.
	 */
	public void setAutowireCandidateResolver( final AutowireCandidateResolver autowireCandidateResolver ) {
		Assert.notNull( autowireCandidateResolver, "AutowireCandidateResolver must not be null" );
		if ( autowireCandidateResolver instanceof BeanFactoryAware ) {
			if ( System.getSecurityManager() != null ) {
				AccessController.doPrivileged( (PrivilegedAction<Object>) () -> {
					( (BeanFactoryAware) autowireCandidateResolver ).setBeanFactory( NewAcrossListableBeanFactory.this );
					return null;
				}, getAccessControlContext() );
			}
			else {
				( (BeanFactoryAware) autowireCandidateResolver ).setBeanFactory( this );
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	/**
	 * Return the autowire candidate resolver for this BeanFactory (never {@code null}).
	 */
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}

	@Override
	public void copyConfigurationFrom( ConfigurableBeanFactory otherFactory ) {
		super.copyConfigurationFrom( otherFactory );
		if ( otherFactory instanceof NewAcrossListableBeanFactory ) {
			NewAcrossListableBeanFactory
					otherListableFactory = (NewAcrossListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			// A clone of the AutowireCandidateResolver since it is potentially BeanFactoryAware...
			setAutowireCandidateResolver(
					BeanUtils.instantiateClass( otherListableFactory.getAutowireCandidateResolver().getClass() ) );
			// Make resolvable dependencies (e.g. ResourceLoader) available here as well...
			this.resolvableDependencies.putAll( otherListableFactory.resolvableDependencies );
		}
	}

	//---------------------------------------------------------------------
	// Implementation of remaining BeanFactory methods
	//---------------------------------------------------------------------

	@Override
	public <T> T getBean( Class<T> requiredType ) throws BeansException {
		return getBean( requiredType, (Object[]) null );
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean( Class<T> requiredType, @Nullable Object... args ) throws BeansException {
		Assert.notNull( requiredType, "Required type must not be null" );
		Object resolved = resolveBean( ResolvableType.forRawClass( requiredType ), args, false );
		if ( resolved == null ) {
			throw new NoSuchBeanDefinitionException( requiredType );
		}
		return (T) resolved;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider( Class<T> requiredType ) throws BeansException {
		Assert.notNull( requiredType, "Required type must not be null" );
		return getBeanProvider( ResolvableType.forRawClass( requiredType ) );
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ObjectProvider<T> getBeanProvider( ResolvableType requiredType ) {
		return new NewAcrossListableBeanFactory.BeanObjectProvider<T>()
		{
			@Override
			public T getObject() throws BeansException {
				T resolved = resolveBean( requiredType, null, false );
				if ( resolved == null ) {
					throw new NoSuchBeanDefinitionException( requiredType );
				}
				return resolved;
			}

			@Override
			public T getObject( Object... args ) throws BeansException {
				T resolved = resolveBean( requiredType, args, false );
				if ( resolved == null ) {
					throw new NoSuchBeanDefinitionException( requiredType );
				}
				return resolved;
			}

			@Override
			@Nullable
			public T getIfAvailable() throws BeansException {
				return resolveBean( requiredType, null, false );
			}

			@Override
			@Nullable
			public T getIfUnique() throws BeansException {
				return resolveBean( requiredType, null, true );
			}

			@Override
			public Stream<T> stream() {
				return Arrays.stream( getBeanNamesForTypedStream( requiredType ) )
				             .map( name -> (T) getBean( name ) )
				             .filter( bean -> !( bean instanceof NullBean ) );
			}

			@Override
			public Stream<T> orderedStream() {
				String[] beanNames = getBeanNamesForTypedStream( requiredType );
				Map<String, T> matchingBeans = new LinkedHashMap<>( beanNames.length );
				for ( String beanName : beanNames ) {
					Object beanInstance = getBean( beanName );
					if ( !( beanInstance instanceof NullBean ) ) {
						matchingBeans.put( beanName, (T) beanInstance );
					}
				}
				Stream<T> stream = matchingBeans.values().stream();
				return stream.sorted( adaptOrderComparator( matchingBeans ) );
			}
		};
	}

	@Nullable
	private <T> T resolveBean( ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull ) {
		NamedBeanHolder<T> namedBean = resolveNamedBean( requiredType, args, nonUniqueAsNull );
		if ( namedBean != null ) {
			return namedBean.getBeanInstance();
		}
		BeanFactory parent = getParentBeanFactory();
		if ( parent instanceof NewAcrossListableBeanFactory ) {
			return ( (NewAcrossListableBeanFactory) parent ).resolveBean( requiredType, args, nonUniqueAsNull );
		}
		else if ( parent != null ) {
			ObjectProvider<T> parentProvider = parent.getBeanProvider( requiredType );
			if ( args != null ) {
				return parentProvider.getObject( args );
			}
			else {
				return ( nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable() );
			}
		}
		return null;
	}

	private String[] getBeanNamesForTypedStream( ResolvableType requiredType ) {
		return BeanFactoryUtils.beanNamesForTypeIncludingAncestors( this, requiredType );
	}

	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public boolean containsBeanDefinition( String beanName ) {
		Assert.notNull( beanName, "Bean name must not be null" );
		return this.beanDefinitionMap.containsKey( beanName );
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		String[] frozenNames = this.frozenBeanDefinitionNames;
		if ( frozenNames != null ) {
			return frozenNames.clone();
		}
		else {
			return StringUtils.toStringArray( this.beanDefinitionNames );
		}
	}

	@Override
	public String[] getBeanNamesForType( ResolvableType type ) {
		Class<?> resolved = type.resolve();
		if ( resolved != null && !type.hasGenerics() ) {
			return getBeanNamesForType( resolved, true, true );
		}
		else {
			return doGetBeanNamesForType( type, true, true );
		}
	}

	@Override
	public String[] getBeanNamesForType( @Nullable Class<?> type ) {
		return getBeanNamesForType( type, true, true );
	}

	@Override
	public String[] getBeanNamesForType( @Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit ) {
		if ( !isConfigurationFrozen() || type == null || !allowEagerInit ) {
			return doGetBeanNamesForType( ResolvableType.forRawClass( type ), includeNonSingletons, allowEagerInit );
		}
		Map<Class<?>, String[]> cache = ( includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType );
		String[] resolvedBeanNames = cache.get( type );
		if ( resolvedBeanNames != null ) {
			return resolvedBeanNames;
		}
		resolvedBeanNames = doGetBeanNamesForType( ResolvableType.forRawClass( type ), includeNonSingletons, true );
		if ( ClassUtils.isCacheSafe( type, getBeanClassLoader() ) ) {
			cache.put( type, resolvedBeanNames );
		}
		return resolvedBeanNames;
	}

	private String[] doGetBeanNamesForType( ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit ) {
		List<String> result = new ArrayList<>();

		boolean localListingMode = isInLocalBeansOperationMode();

		// Check all bean definitions.
		for ( String beanName : this.beanDefinitionNames ) {
			// Only consider bean as eligible if the bean name
			// is not defined as alias for some other bean.
			if ( !isAlias( beanName ) ) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition( beanName );

					if ( localListingMode && mbd instanceof ExposedBeanDefinition ) {
						// Skip exposed beans if local listing mode is active
						continue;
					}

					// Only check bean definition if it is complete.
					if ( !mbd.isAbstract() && ( allowEagerInit ||
							( mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading() ) &&
									!requiresEagerInitForType( mbd.getFactoryBeanName() ) ) ) {
						// In case of FactoryBean, match object created by FactoryBean.
						boolean isFactoryBean = isFactoryBean( beanName, mbd );
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
						boolean matchFound =
								( allowEagerInit || !isFactoryBean ||
										( dbd != null && !mbd.isLazyInit() ) || containsSingleton( beanName ) ) &&
										( includeNonSingletons ||
												( dbd != null ? mbd.isSingleton() : isSingleton( beanName ) ) ) &&
										isTypeMatch( beanName, type );
						if ( !matchFound && isFactoryBean ) {
							// In case of FactoryBean, try to match FactoryBean instance itself next.
							beanName = FACTORY_BEAN_PREFIX + beanName;
							matchFound = ( includeNonSingletons || mbd.isSingleton() ) && isTypeMatch( beanName, type );
						}
						if ( matchFound ) {
							result.add( beanName );
						}
					}
				}
				catch ( CannotLoadBeanClassException ex ) {
					if ( allowEagerInit ) {
						throw ex;
					}
					// Probably a class name with a placeholder: let's ignore it for type matching purposes.
					if ( logger.isTraceEnabled() ) {
						logger.trace( "Ignoring bean class loading failure for bean '" + beanName + "'", ex );
					}
					onSuppressedException( ex );
				}
				catch ( BeanDefinitionStoreException ex ) {
					if ( allowEagerInit ) {
						throw ex;
					}
					// Probably some metadata with a placeholder: let's ignore it for type matching purposes.
					if ( logger.isTraceEnabled() ) {
						logger.trace( "Ignoring unresolvable metadata in bean definition '" + beanName + "'", ex );
					}
					onSuppressedException( ex );
				}
			}
		}

		// Check manually registered singletons too.
		for ( String beanName : this.manualSingletonNames ) {
			try {
				// In case of FactoryBean, match object created by FactoryBean.
				if ( isFactoryBean( beanName ) ) {
					if ( ( includeNonSingletons || isSingleton( beanName ) ) && isTypeMatch( beanName, type ) ) {
						result.add( beanName );
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if ( isTypeMatch( beanName, type ) ) {
					result.add( beanName );
				}
			}
			catch ( NoSuchBeanDefinitionException ex ) {
				// Shouldn't happen - probably a result of circular reference resolution...
				if ( logger.isTraceEnabled() ) {
					logger.trace( "Failed to check manually registered singleton with name '" + beanName + "'", ex );
				}
			}
		}

		return StringUtils.toStringArray( result );
	}

	/**
	 * Check whether the specified bean would need to be eagerly initialized
	 * in order to determine its type.
	 *
	 * @param factoryBeanName a factory-bean reference that the bean definition
	 *                        defines a factory method for
	 * @return whether eager initialization is necessary
	 */
	private boolean requiresEagerInitForType( @Nullable String factoryBeanName ) {
		return ( factoryBeanName != null && isFactoryBean( factoryBeanName ) && !containsSingleton( factoryBeanName ) );
	}

	@Override
	public <T> Map<String, T> getBeansOfType( @Nullable Class<T> type ) throws BeansException {
		return getBeansOfType( type, true, true );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfType( @Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit )
			throws BeansException {

		String[] beanNames = getBeanNamesForType( type, includeNonSingletons, allowEagerInit );
		Map<String, T> result = new LinkedHashMap<>( beanNames.length );
		for ( String beanName : beanNames ) {
			try {
				Object beanInstance = getBean( beanName );
				if ( !( beanInstance instanceof NullBean ) ) {
					result.put( beanName, (T) beanInstance );
				}
			}
			catch ( BeanCreationException ex ) {
				Throwable rootCause = ex.getMostSpecificCause();
				if ( rootCause instanceof BeanCurrentlyInCreationException ) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					String exBeanName = bce.getBeanName();
					if ( exBeanName != null && isCurrentlyInCreation( exBeanName ) ) {
						if ( logger.isTraceEnabled() ) {
							logger.trace( "Ignoring match to currently created bean '" + exBeanName + "': " +
									              ex.getMessage() );
						}
						onSuppressedException( ex );
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	@Override
	public String[] getBeanNamesForAnnotation( Class<? extends Annotation> annotationType ) {
		List<String> result = new ArrayList<>();
		boolean localListingMode = isInLocalBeansOperationMode();
		for ( String beanName : this.beanDefinitionNames ) {
			BeanDefinition beanDefinition = getBeanDefinition( beanName );
			if ( localListingMode && beanDefinition instanceof ExposedBeanDefinition ) {
				// Skip exposed beans if local listing mode is active
				continue;
			}
			if ( !beanDefinition.isAbstract() && findAnnotationOnBean( beanName, annotationType ) != null ) {
				result.add( beanName );
			}
		}
		for ( String beanName : this.manualSingletonNames ) {
			if ( !result.contains( beanName ) && findAnnotationOnBean( beanName, annotationType ) != null ) {
				result.add( beanName );
			}
		}
		return StringUtils.toStringArray( result );
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation( Class<? extends Annotation> annotationType ) {
		String[] beanNames = getBeanNamesForAnnotation( annotationType );
		Map<String, Object> result = new LinkedHashMap<>( beanNames.length );
		for ( String beanName : beanNames ) {
			Object beanInstance = getBean( beanName );
			if ( !( beanInstance instanceof NullBean ) ) {
				result.put( beanName, beanInstance );
			}
		}
		return result;
	}

	@Override
	@Nullable
	public <A extends Annotation> A findAnnotationOnBean( String beanName, Class<A> annotationType )
			throws NoSuchBeanDefinitionException {

		A ann = null;
		Class<?> beanType = getType( beanName );
		if ( beanType != null ) {
			ann = AnnotationUtils.findAnnotation( beanType, annotationType );
		}
		if ( ann == null && containsBeanDefinition( beanName ) ) {
			// Check raw bean class, e.g. in case of a proxy.
			RootBeanDefinition bd = getMergedLocalBeanDefinition( beanName );
			if ( bd.hasBeanClass() ) {
				Class<?> beanClass = bd.getBeanClass();
				if ( beanClass != beanType ) {
					ann = AnnotationUtils.findAnnotation( beanClass, annotationType );
				}
			}
		}
		return ann;
	}

	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory interface
	//---------------------------------------------------------------------

	@Override
	public void registerResolvableDependency( Class<?> dependencyType, @Nullable Object autowiredValue ) {
		Assert.notNull( dependencyType, "Dependency type must not be null" );
		if ( autowiredValue != null ) {
			if ( !( autowiredValue instanceof ObjectFactory || dependencyType.isInstance( autowiredValue ) ) ) {
				throw new IllegalArgumentException( "Value [" + autowiredValue +
						                                    "] does not implement specified dependency type [" + dependencyType.getName() + "]" );
			}
			this.resolvableDependencies.put( dependencyType, autowiredValue );
		}
	}

	@Override
	public boolean isAutowireCandidate( String beanName, DependencyDescriptor descriptor )
			throws NoSuchBeanDefinitionException {

		return isAutowireCandidate( beanName, descriptor, getAutowireCandidateResolver() );
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 *
	 * @param beanName   the name of the bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver   the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate( String beanName, DependencyDescriptor descriptor, AutowireCandidateResolver resolver )
			throws NoSuchBeanDefinitionException {

		String beanDefinitionName = BeanFactoryUtils.transformedBeanName( beanName );
		if ( containsBeanDefinition( beanDefinitionName ) ) {
			return isAutowireCandidate( beanName, getMergedLocalBeanDefinition( beanDefinitionName ), descriptor, resolver );
		}
		else if ( containsSingleton( beanName ) ) {
			return isAutowireCandidate( beanName, new RootBeanDefinition( getType( beanName ) ), descriptor, resolver );
		}

		BeanFactory parent = getParentBeanFactory();
		if ( parent instanceof DefaultListableBeanFactory ) {
			// No bean definition found in this factory -> delegate to parent.
			return ( (DefaultListableBeanFactory) parent ).isAutowireCandidate( beanName, descriptor, resolver );
		}
		else if ( parent instanceof ConfigurableListableBeanFactory ) {
			// If no DefaultListableBeanFactory, can't pass the resolver along.
			return ( (ConfigurableListableBeanFactory) parent ).isAutowireCandidate( beanName, descriptor );
		}
		else {
			return true;
		}
	}

	/**
	 * Determine whether the specified bean definition qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 *
	 * @param beanName   the name of the bean definition to check
	 * @param mbd        the merged bean definition to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @param resolver   the AutowireCandidateResolver to use for the actual resolution algorithm
	 * @return whether the bean should be considered as autowire candidate
	 */
	protected boolean isAutowireCandidate( String beanName, RootBeanDefinition mbd,
	                                       DependencyDescriptor descriptor, AutowireCandidateResolver resolver ) {

		String beanDefinitionName = BeanFactoryUtils.transformedBeanName( beanName );
		resolveBeanClass( mbd, beanDefinitionName );
		if ( mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null ) {
			new ConstructorResolver( this ).resolveFactoryMethodIfPossible( mbd );
		}
		return resolver.isAutowireCandidate(
				new BeanDefinitionHolder( mbd, beanName, getAliases( beanDefinitionName ) ), descriptor );
	}

	@Override
	public BeanDefinition getBeanDefinition( String beanName ) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get( beanName );
		if ( bd == null ) {
			if ( logger.isTraceEnabled() ) {
				logger.trace( "No bean named '" + beanName + "' found in " + this );
			}
			throw new NoSuchBeanDefinitionException( beanName );
		}
		return bd;
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<>();
		iterator.add( this.beanDefinitionNames.iterator() );
		iterator.add( this.manualSingletonNames.iterator() );
		return iterator;
	}

	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		clearByTypeCache();
	}

	@Override
	public void freezeConfiguration() {
		// frozen configuration is not possible
		//this.configurationFrozen = true;
		//this.frozenBeanDefinitionNames = StringUtils.toStringArray( this.beanDefinitionNames );
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	/**
	 * Considers all beans as eligible for metadata caching
	 * if the factory's configuration has been marked as frozen.
	 *
	 * @see #freezeConfiguration()
	 */
	@Override
	protected boolean isBeanEligibleForMetadataCaching( String beanName ) {
		return ( this.configurationFrozen || super.isBeanEligibleForMetadataCaching( beanName ) );
	}

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if ( logger.isTraceEnabled() ) {
			logger.trace( "Pre-instantiating singletons in " + this );
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		List<String> beanNames = new ArrayList<>( this.beanDefinitionNames );

		// Trigger initialization of all non-lazy singleton beans...
		for ( String beanName : beanNames ) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition( beanName );
			if ( bd instanceof ExposedBeanDefinition ) {
				// exposed beans are skipped from early instantiation
				continue;
			}
			if ( !bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit() ) {
				if ( isFactoryBean( beanName ) ) {
					Object bean = getBean( FACTORY_BEAN_PREFIX + beanName );
					if ( bean instanceof FactoryBean ) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
						boolean isEagerInit;
						if ( System.getSecurityManager() != null && factory instanceof SmartFactoryBean ) {
							isEagerInit = AccessController.doPrivileged( (PrivilegedAction<Boolean>)
									                                             ( (SmartFactoryBean<?>) factory )::isEagerInit,
							                                             getAccessControlContext() );
						}
						else {
							isEagerInit = ( factory instanceof SmartFactoryBean &&
									( (SmartFactoryBean<?>) factory ).isEagerInit() );
						}
						if ( isEagerInit ) {
							getBean( beanName );
						}
					}
				}
				else {
					getBean( beanName );
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
		for ( String beanName : beanNames ) {
			Object singletonInstance = getSingleton( beanName );
			if ( singletonInstance instanceof SmartInitializingSingleton ) {
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if ( System.getSecurityManager() != null ) {
					AccessController.doPrivileged( (PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext() );
				}
				else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}

	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry interface
	//---------------------------------------------------------------------

	@Override
	public void registerBeanDefinition( String beanName, BeanDefinition beanDefinition )
			throws BeanDefinitionStoreException {

		Assert.hasText( beanName, "Bean name must not be empty" );
		Assert.notNull( beanDefinition, "BeanDefinition must not be null" );

		if ( beanDefinition instanceof AbstractBeanDefinition ) {
			try {
				( (AbstractBeanDefinition) beanDefinition ).validate();
			}
			catch ( BeanDefinitionValidationException ex ) {
				throw new BeanDefinitionStoreException( beanDefinition.getResourceDescription(), beanName,
				                                        "Validation of bean definition failed", ex );
			}
		}

		BeanDefinition existingDefinition = this.beanDefinitionMap.get( beanName );
		if ( existingDefinition != null ) {
			if ( !isAllowBeanDefinitionOverriding() ) {
				throw new BeanDefinitionOverrideException( beanName, beanDefinition, existingDefinition );
			}
			else if ( existingDefinition.getRole() < beanDefinition.getRole() ) {
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				if ( logger.isInfoEnabled() ) {
					logger.info( "Overriding user-defined bean definition for bean '" + beanName +
							             "' with a framework-generated bean definition: replacing [" +
							             existingDefinition + "] with [" + beanDefinition + "]" );
				}
			}
			else if ( !beanDefinition.equals( existingDefinition ) ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug( "Overriding bean definition for bean '" + beanName +
							              "' with a different definition: replacing [" + existingDefinition +
							              "] with [" + beanDefinition + "]" );
				}
			}
			else {
				if ( logger.isTraceEnabled() ) {
					logger.trace( "Overriding bean definition for bean '" + beanName +
							              "' with an equivalent definition: replacing [" + existingDefinition +
							              "] with [" + beanDefinition + "]" );
				}
			}
			this.beanDefinitionMap.put( beanName, beanDefinition );
		}
		else {
			if ( hasBeanCreationStarted() ) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
				synchronized ( this.beanDefinitionMap ) {
					this.beanDefinitionMap.put( beanName, beanDefinition );
					List<String> updatedDefinitions = new ArrayList<>( this.beanDefinitionNames.size() + 1 );
					updatedDefinitions.addAll( this.beanDefinitionNames );
					updatedDefinitions.add( beanName );
					this.beanDefinitionNames = updatedDefinitions;
					removeManualSingletonName( beanName );
				}
			}
			else {
				// Still in startup registration phase
				this.beanDefinitionMap.put( beanName, beanDefinition );
				this.beanDefinitionNames.add( beanName );
				removeManualSingletonName( beanName );
			}
			this.frozenBeanDefinitionNames = null;
		}

		if ( existingDefinition != null || containsSingleton( beanName ) ) {
			resetBeanDefinition( beanName );
		}
	}

	@Override
	public void removeBeanDefinition( String beanName ) throws NoSuchBeanDefinitionException {
		Assert.hasText( beanName, "'beanName' must not be empty" );

		BeanDefinition bd = this.beanDefinitionMap.remove( beanName );
		if ( bd == null ) {
			if ( logger.isTraceEnabled() ) {
				logger.trace( "No bean named '" + beanName + "' found in " + this );
			}
			throw new NoSuchBeanDefinitionException( beanName );
		}

		if ( hasBeanCreationStarted() ) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized ( this.beanDefinitionMap ) {
				List<String> updatedDefinitions = new ArrayList<>( this.beanDefinitionNames );
				updatedDefinitions.remove( beanName );
				this.beanDefinitionNames = updatedDefinitions;
			}
		}
		else {
			// Still in startup registration phase
			this.beanDefinitionNames.remove( beanName );
		}
		this.frozenBeanDefinitionNames = null;

		resetBeanDefinition( beanName );
	}

	/**
	 * Reset all bean definition caches for the given bean,
	 * including the caches of beans that are derived from it.
	 * <p>Called after an existing bean definition has been replaced or removed,
	 * triggering {@link #clearMergedBeanDefinition}, {@link #destroySingleton}
	 * and {@link MergedBeanDefinitionPostProcessor#resetBeanDefinition} on the
	 * given bean and on all bean definitions that have the given bean as parent.
	 *
	 * @param beanName the name of the bean to reset
	 * @see #registerBeanDefinition
	 * @see #removeBeanDefinition
	 */
	protected void resetBeanDefinition( String beanName ) {
		// Remove the merged bean definition for the given bean, if already created.
		clearMergedBeanDefinition( beanName );

		// Remove corresponding bean from singleton cache, if any. Shouldn't usually
		// be necessary, rather just meant for overriding a context's default beans
		// (e.g. the default StaticMessageSource in a StaticApplicationContext).
		destroySingleton( beanName );

		// Notify all post-processors that the specified bean definition has been reset.
		for ( BeanPostProcessor processor : getBeanPostProcessors() ) {
			if ( processor instanceof MergedBeanDefinitionPostProcessor ) {
				( (MergedBeanDefinitionPostProcessor) processor ).resetBeanDefinition( beanName );
			}
		}

		// Reset all bean definitions that have the given bean as parent (recursively).
		for ( String bdName : this.beanDefinitionNames ) {
			if ( !beanName.equals( bdName ) ) {
				BeanDefinition bd = this.beanDefinitionMap.get( bdName );
				// Ensure bd is non-null due to potential concurrent modification
				// of the beanDefinitionMap.
				if ( bd != null && beanName.equals( bd.getParentName() ) ) {
					resetBeanDefinition( bdName );
				}
			}
		}
	}

	/**
	 * Only allows alias overriding if bean definition overriding is allowed.
	 */
	@Override
	protected boolean allowAliasOverriding() {
		return isAllowBeanDefinitionOverriding();
	}

	@Override
	public void registerSingleton( String beanName, Object singletonObject ) throws IllegalStateException {
		super.registerSingleton( beanName, singletonObject );
		updateManualSingletonNames( set -> set.add( beanName ), set -> !this.beanDefinitionMap.containsKey( beanName ) );
		clearByTypeCache();
	}

	@Override
	public void destroySingletons() {
		super.destroySingletons();
		updateManualSingletonNames( Set::clear, set -> !set.isEmpty() );
		clearByTypeCache();
	}

	@Override
	public void destroySingleton( String beanName ) {
		super.destroySingleton( beanName );
		removeManualSingletonName( beanName );
		clearByTypeCache();
	}

	private void removeManualSingletonName( String beanName ) {
		updateManualSingletonNames( set -> set.remove( beanName ), set -> set.contains( beanName ) );
	}

	/**
	 * Update the factory's internal set of manual singleton names.
	 *
	 * @param action    the modification action
	 * @param condition a precondition for the modification action
	 *                  (if this condition does not apply, the action can be skipped)
	 */
	private void updateManualSingletonNames( Consumer<Set<String>> action, Predicate<Set<String>> condition ) {
		if ( hasBeanCreationStarted() ) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized ( this.beanDefinitionMap ) {
				if ( condition.test( this.manualSingletonNames ) ) {
					Set<String> updatedSingletons = new LinkedHashSet<>( this.manualSingletonNames );
					action.accept( updatedSingletons );
					this.manualSingletonNames = updatedSingletons;
				}
			}
		}
		else {
			// Still in startup registration phase
			if ( condition.test( this.manualSingletonNames ) ) {
				action.accept( this.manualSingletonNames );
			}
		}
	}

	/**
	 * Remove any assumptions about by-type mappings.
	 */
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}

	//---------------------------------------------------------------------
	// Dependency resolution functionality
	//---------------------------------------------------------------------

	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean( Class<T> requiredType ) throws BeansException {
		Assert.notNull( requiredType, "Required type must not be null" );
		NamedBeanHolder<T> namedBean = resolveNamedBean( ResolvableType.forRawClass( requiredType ), null, false );
		if ( namedBean != null ) {
			return namedBean;
		}
		BeanFactory parent = getParentBeanFactory();
		if ( parent instanceof AutowireCapableBeanFactory ) {
			return ( (AutowireCapableBeanFactory) parent ).resolveNamedBean( requiredType );
		}
		throw new NoSuchBeanDefinitionException( requiredType );
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(
			ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull ) throws BeansException {

		Assert.notNull( requiredType, "Required type must not be null" );
		String[] candidateNames = getBeanNamesForType( requiredType );

		if ( candidateNames.length > 1 ) {
			List<String> autowireCandidates = new ArrayList<>( candidateNames.length );
			for ( String beanName : candidateNames ) {
				if ( !containsBeanDefinition( beanName ) || getBeanDefinition( beanName ).isAutowireCandidate() ) {
					autowireCandidates.add( beanName );
				}
			}
			if ( !autowireCandidates.isEmpty() ) {
				candidateNames = StringUtils.toStringArray( autowireCandidates );
			}
		}

		if ( candidateNames.length == 1 ) {
			String beanName = candidateNames[0];
			return new NamedBeanHolder<>( beanName, (T) getBean( beanName, requiredType.toClass(), args ) );
		}
		else if ( candidateNames.length > 1 ) {
			Map<String, Object> candidates = new LinkedHashMap<>( candidateNames.length );
			for ( String beanName : candidateNames ) {
				if ( containsSingleton( beanName ) && args == null ) {
					Object beanInstance = getBean( beanName );
					candidates.put( beanName, ( beanInstance instanceof NullBean ? null : beanInstance ) );
				}
				else {
					candidates.put( beanName, getType( beanName ) );
				}
			}
			String candidateName = determinePrimaryCandidate( candidates, requiredType.toClass() );
			if ( candidateName == null ) {
				candidateName = determineHighestPriorityCandidate( candidates, requiredType.toClass() );
			}
			if ( candidateName != null ) {
				Object beanInstance = candidates.get( candidateName );
				if ( beanInstance == null || beanInstance instanceof Class ) {
					beanInstance = getBean( candidateName, requiredType.toClass(), args );
				}
				return new NamedBeanHolder<>( candidateName, (T) beanInstance );
			}
			if ( !nonUniqueAsNull ) {
				throw new NoUniqueBeanDefinitionException( requiredType, candidates.keySet() );
			}
		}

		return null;
	}

	@Override
	@Nullable
	public Object resolveDependency( DependencyDescriptor descriptor, @Nullable String requestingBeanName,
	                                 @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter ) throws BeansException {

		descriptor.initParameterNameDiscovery( getParameterNameDiscoverer() );
		if ( Optional.class == descriptor.getDependencyType() ) {
			return createOptionalDependency( descriptor, requestingBeanName );
		}
		else if ( ObjectFactory.class == descriptor.getDependencyType() ||
				ObjectProvider.class == descriptor.getDependencyType() ) {
			return new NewAcrossListableBeanFactory.DependencyObjectProvider( descriptor, requestingBeanName );
		}
		else if ( javaxInjectProviderClass == descriptor.getDependencyType() ) {
			return new NewAcrossListableBeanFactory.Jsr330Factory().createDependencyProvider( descriptor, requestingBeanName );
		}
		else {
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
					descriptor, requestingBeanName );
			if ( result == null ) {
				result = doResolveDependency( descriptor, requestingBeanName, autowiredBeanNames, typeConverter );
			}
			return result;
		}
	}

	@Nullable
	public Object doResolveDependency( DependencyDescriptor descriptor, @Nullable String beanName,
	                                   @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter ) throws BeansException {

		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint( descriptor );
		try {
			Object shortcut = descriptor.resolveShortcut( this );
			if ( shortcut != null ) {
				return shortcut;
			}

			Class<?> type = descriptor.getDependencyType();
			Object value = getAutowireCandidateResolver().getSuggestedValue( descriptor );
			if ( value != null ) {
				if ( value instanceof String ) {
					String strVal = resolveEmbeddedValue( (String) value );
					BeanDefinition bd = ( beanName != null && containsBean( beanName ) ?
							getMergedBeanDefinition( beanName ) : null );
					value = evaluateBeanDefinitionString( strVal, bd );
				}
				TypeConverter converter = ( typeConverter != null ? typeConverter : getTypeConverter() );
				try {
					return converter.convertIfNecessary( value, type, descriptor.getTypeDescriptor() );
				}
				catch ( UnsupportedOperationException ex ) {
					// A custom TypeConverter which does not support TypeDescriptor resolution...
					return ( descriptor.getField() != null ?
							converter.convertIfNecessary( value, type, descriptor.getField() ) :
							converter.convertIfNecessary( value, type, descriptor.getMethodParameter() ) );
				}
			}

			Object multipleBeans = resolveMultipleBeans( descriptor, beanName, autowiredBeanNames, typeConverter );
			if ( multipleBeans != null ) {
				return multipleBeans;
			}

			Map<String, Object> matchingBeans = findAutowireCandidates( beanName, type, descriptor );
			if ( matchingBeans.isEmpty() ) {
				if ( isRequired( descriptor ) ) {
					raiseNoMatchingBeanFound( type, descriptor.getResolvableType(), descriptor );
				}
				return null;
			}

			String autowiredBeanName;
			Object instanceCandidate;

			if ( matchingBeans.size() > 1 ) {
				autowiredBeanName = determineAutowireCandidate( matchingBeans, descriptor );
				if ( autowiredBeanName == null ) {
					if ( isRequired( descriptor ) || !indicatesMultipleBeans( type ) ) {
						return descriptor.resolveNotUnique( descriptor.getResolvableType(), matchingBeans );
					}
					else {
						// In case of an optional Collection/Map, silently ignore a non-unique case:
						// possibly it was meant to be an empty collection of multiple regular beans
						// (before 4.3 in particular when we didn't even look for collection beans).
						return null;
					}
				}
				instanceCandidate = matchingBeans.get( autowiredBeanName );
			}
			else {
				// We have exactly one match.
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				autowiredBeanName = entry.getKey();
				instanceCandidate = entry.getValue();
			}

			if ( autowiredBeanNames != null ) {
				autowiredBeanNames.add( autowiredBeanName );
			}
			if ( instanceCandidate instanceof Class ) {
				instanceCandidate = descriptor.resolveCandidate( autowiredBeanName, type, this );
			}
			Object result = instanceCandidate;
			if ( result instanceof NullBean ) {
				if ( isRequired( descriptor ) ) {
					raiseNoMatchingBeanFound( type, descriptor.getResolvableType(), descriptor );
				}
				result = null;
			}
			if ( !ClassUtils.isAssignableValue( type, result ) ) {
				throw new BeanNotOfRequiredTypeException( autowiredBeanName, type, instanceCandidate.getClass() );
			}
			return result;
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint( previousInjectionPoint );
		}
	}

	@Nullable
	private Object resolveMultipleBeans( DependencyDescriptor descriptor, @Nullable String beanName,
	                                     @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter ) {

		final Class<?> type = descriptor.getDependencyType();

		if ( descriptor instanceof NewAcrossListableBeanFactory.StreamDependencyDescriptor ) {
			Map<String, Object> matchingBeans = findAutowireCandidates( beanName, type, descriptor );
			if ( autowiredBeanNames != null ) {
				autowiredBeanNames.addAll( matchingBeans.keySet() );
			}
			Stream<Object> stream = matchingBeans.keySet().stream()
			                                     .map( name -> descriptor.resolveCandidate( name, type, this ) )
			                                     .filter( bean -> !( bean instanceof NullBean ) );
			if ( ( (NewAcrossListableBeanFactory.StreamDependencyDescriptor) descriptor ).isOrdered() ) {
				stream = stream.sorted( adaptOrderComparator( matchingBeans ) );
			}
			return stream;
		}
		else if ( type.isArray() ) {
			Class<?> componentType = type.getComponentType();
			ResolvableType resolvableType = descriptor.getResolvableType();
			Class<?> resolvedArrayType = resolvableType.resolve( type );
			if ( resolvedArrayType != type ) {
				componentType = resolvableType.getComponentType().resolve();
			}
			if ( componentType == null ) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates( beanName, componentType,
			                                                            new NewAcrossListableBeanFactory.MultiElementDescriptor( descriptor ) );
			if ( matchingBeans.isEmpty() ) {
				return null;
			}
			if ( autowiredBeanNames != null ) {
				autowiredBeanNames.addAll( matchingBeans.keySet() );
			}
			TypeConverter converter = ( typeConverter != null ? typeConverter : getTypeConverter() );
			Object result = converter.convertIfNecessary( matchingBeans.values(), resolvedArrayType );
			if ( result instanceof Object[] ) {
				Comparator<Object> comparator = adaptDependencyComparator( matchingBeans );
				if ( comparator != null ) {
					Arrays.sort( (Object[]) result, comparator );
				}
			}
			return result;
		}
		else if ( Collection.class.isAssignableFrom( type ) && type.isInterface() ) {
			Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
			if ( elementType == null ) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates( beanName, elementType,
			                                                            new NewAcrossListableBeanFactory.MultiElementDescriptor( descriptor ) );
			if ( matchingBeans.isEmpty() ) {
				return null;
			}
			if ( autowiredBeanNames != null ) {
				autowiredBeanNames.addAll( matchingBeans.keySet() );
			}
			TypeConverter converter = ( typeConverter != null ? typeConverter : getTypeConverter() );
			Object result = converter.convertIfNecessary( matchingBeans.values(), type );
			if ( result instanceof List ) {
				Comparator<Object> comparator = adaptDependencyComparator( matchingBeans );
				if ( comparator != null ) {
					( (List<?>) result ).sort( comparator );
				}
			}
			return result;
		}
		else if ( Map.class == type ) {
			ResolvableType mapType = descriptor.getResolvableType().asMap();
			Class<?> keyType = mapType.resolveGeneric( 0 );
			if ( String.class != keyType ) {
				return null;
			}
			Class<?> valueType = mapType.resolveGeneric( 1 );
			if ( valueType == null ) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates( beanName, valueType,
			                                                            new NewAcrossListableBeanFactory.MultiElementDescriptor( descriptor ) );
			if ( matchingBeans.isEmpty() ) {
				return null;
			}
			if ( autowiredBeanNames != null ) {
				autowiredBeanNames.addAll( matchingBeans.keySet() );
			}
			return matchingBeans;
		}
		else {
			return null;
		}
	}

	private boolean isRequired( DependencyDescriptor descriptor ) {
		return getAutowireCandidateResolver().isRequired( descriptor );
	}

	private boolean indicatesMultipleBeans( Class<?> type ) {
		return ( type.isArray() || ( type.isInterface() && ( Collection.class.isAssignableFrom( type ) || Map.class.isAssignableFrom( type ) ) ) );
	}

	@Nullable
	private Comparator<Object> adaptDependencyComparator( Map<String, ?> matchingBeans ) {
		Comparator<Object> comparator = getDependencyComparator();
		if ( comparator instanceof OrderComparator ) {
			return ( (OrderComparator) comparator ).withSourceProvider(
					createFactoryAwareOrderSourceProvider( matchingBeans ) );
		}
		else {
			return comparator;
		}
	}

	private Comparator<Object> adaptOrderComparator( Map<String, ?> matchingBeans ) {
		Comparator<Object> dependencyComparator = getDependencyComparator();
		OrderComparator comparator = ( dependencyComparator instanceof OrderComparator ?
				(OrderComparator) dependencyComparator : OrderComparator.INSTANCE );
		return comparator.withSourceProvider( createFactoryAwareOrderSourceProvider( matchingBeans ) );
	}

	private OrderComparator.OrderSourceProvider createFactoryAwareOrderSourceProvider( Map<String, ?> beans ) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<>();
		beans.forEach( ( beanName, instance ) -> instancesToBeanNames.put( instance, beanName ) );
		return new NewAcrossListableBeanFactory.FactoryAwareOrderSourceProvider( instancesToBeanNames );
	}

	/**
	 * Find bean instances that match the required type.
	 * Called during autowiring for the specified bean.
	 *
	 * @param beanName     the name of the bean that is about to be wired
	 * @param requiredType the actual type of bean to look for
	 *                     (may be an array component type or collection element type)
	 * @param descriptor   the descriptor of the dependency to resolve
	 * @return a Map of candidate names and candidate instances that match
	 * the required type (never {@code null})
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected Map<String, Object> findAutowireCandidates(
			@Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor ) {

		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager() );
		Map<String, Object> result = new LinkedHashMap<>( candidateNames.length );
		for ( Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet() ) {
			Class<?> autowiringType = classObjectEntry.getKey();
			if ( autowiringType.isAssignableFrom( requiredType ) ) {
				Object autowiringValue = classObjectEntry.getValue();
				autowiringValue = AutowireUtils.resolveAutowiringValue( autowiringValue, requiredType );
				if ( requiredType.isInstance( autowiringValue ) ) {
					result.put( ObjectUtils.identityToString( autowiringValue ), autowiringValue );
					break;
				}
			}
		}
		for ( String candidate : candidateNames ) {
			if ( !isSelfReference( beanName, candidate ) && isAutowireCandidate( candidate, descriptor ) ) {
				addCandidateEntry( result, candidate, descriptor, requiredType );
			}
		}
		if ( result.isEmpty() ) {
			boolean multiple = indicatesMultipleBeans( requiredType );
			// Consider fallback matches if the first pass failed to find anything...
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for ( String candidate : candidateNames ) {
				if ( !isSelfReference( beanName, candidate ) && isAutowireCandidate( candidate, fallbackDescriptor ) &&
						( !multiple || getAutowireCandidateResolver().hasQualifier( descriptor ) ) ) {
					addCandidateEntry( result, candidate, descriptor, requiredType );
				}
			}
			if ( result.isEmpty() && !multiple ) {
				// Consider self references as a final pass...
				// but in the case of a dependency collection, not the very same bean itself.
				for ( String candidate : candidateNames ) {
					if ( isSelfReference( beanName, candidate ) &&
							( !( descriptor instanceof NewAcrossListableBeanFactory.MultiElementDescriptor ) || !beanName.equals( candidate ) ) &&
							isAutowireCandidate( candidate, fallbackDescriptor ) ) {
						addCandidateEntry( result, candidate, descriptor, requiredType );
					}
				}
			}
		}
		return result;
	}

	/**
	 * Add an entry to the candidate map: a bean instance if available or just the resolved
	 * type, preventing early bean initialization ahead of primary candidate selection.
	 */
	private void addCandidateEntry( Map<String, Object> candidates, String candidateName,
	                                DependencyDescriptor descriptor, Class<?> requiredType ) {

		if ( descriptor instanceof NewAcrossListableBeanFactory.MultiElementDescriptor ) {
			Object beanInstance = descriptor.resolveCandidate( candidateName, requiredType, this );
			if ( !( beanInstance instanceof NullBean ) ) {
				candidates.put( candidateName, beanInstance );
			}
		}
		else if ( containsSingleton( candidateName ) || ( descriptor instanceof NewAcrossListableBeanFactory.StreamDependencyDescriptor &&
				( (NewAcrossListableBeanFactory.StreamDependencyDescriptor) descriptor ).isOrdered() ) ) {
			Object beanInstance = descriptor.resolveCandidate( candidateName, requiredType, this );
			candidates.put( candidateName, ( beanInstance instanceof NullBean ? null : beanInstance ) );
		}
		else {
			candidates.put( candidateName, getType( candidateName ) );
		}
	}

	/**
	 * Determine the autowire candidate in the given set of beans.
	 * <p>Looks for {@code @Primary} and {@code @Priority} (in that order).
	 *
	 * @param candidates a Map of candidate names and candidate instances
	 *                   that match the required type, as returned by {@link #findAutowireCandidates}
	 * @param descriptor the target dependency to match against
	 * @return the name of the autowire candidate, or {@code null} if none found
	 */
	@Nullable
	protected String determineAutowireCandidate( Map<String, Object> candidates, DependencyDescriptor descriptor ) {
		Class<?> requiredType = descriptor.getDependencyType();
		String primaryCandidate = determinePrimaryCandidate( candidates, requiredType );
		if ( primaryCandidate != null ) {
			return primaryCandidate;
		}
		String priorityCandidate = determineHighestPriorityCandidate( candidates, requiredType );
		if ( priorityCandidate != null ) {
			return priorityCandidate;
		}
		// Fallback
		for ( Map.Entry<String, Object> entry : candidates.entrySet() ) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			if ( ( beanInstance != null && this.resolvableDependencies.containsValue( beanInstance ) ) ||
					matchesBeanName( candidateName, descriptor.getDependencyName() ) ) {
				return candidateName;
			}
		}
		return null;
	}

	/**
	 * Determine the primary candidate in the given set of beans.
	 *
	 * @param candidates   a Map of candidate names and candidate instances
	 *                     (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the primary candidate, or {@code null} if none found
	 * @see #isPrimary(String, Object)
	 */
	@Nullable
	protected String determinePrimaryCandidate( Map<String, Object> candidates, Class<?> requiredType ) {
		String primaryBeanName = null;
		boolean nonExposedCandidatePossible = true;
		for ( Map.Entry<String, Object> entry : candidates.entrySet() ) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			BeanDefinition localBeanDefinition = beanDefinitionMap.get( candidateBeanName );
			boolean candidateIsExposed = localBeanDefinition instanceof ExposedBeanDefinition;
			boolean candidateLocal = localBeanDefinition != null;

			if ( candidateLocal && !candidateIsExposed && nonExposedCandidatePossible ) {
				// a single - non-exposed bean definition is primary versus an exposed bean definition.
				if ( primaryBeanName == null ) {
					primaryBeanName = candidateBeanName;
					continue;
				}
				else {
					primaryBeanName = null;
					nonExposedCandidatePossible = false;
				}
			}

			if ( isPrimary( candidateBeanName, beanInstance ) ) {
				if ( primaryBeanName != null ) {
					boolean primaryLocal = containsBeanDefinition( primaryBeanName );
					if ( candidateLocal && primaryLocal ) {
						throw new NoUniqueBeanDefinitionException( requiredType, candidates.size(),
						                                           "more than one 'primary' bean found among candidates: " + candidates.keySet() );
					}
					else if ( candidateLocal ) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					primaryBeanName = candidateBeanName;
				}
			}
		}
		return primaryBeanName;
	}

	/**
	 * Determine the candidate with the highest priority in the given set of beans.
	 * <p>Based on {@code @javax.annotation.Priority}. As defined by the related
	 * {@link org.springframework.core.Ordered} interface, the lowest value has
	 * the highest priority.
	 *
	 * @param candidates   a Map of candidate names and candidate instances
	 *                     (or candidate classes if not created yet) that match the required type
	 * @param requiredType the target dependency type to match against
	 * @return the name of the candidate with the highest priority,
	 * or {@code null} if none found
	 * @see #getPriority(Object)
	 */
	@Nullable
	protected String determineHighestPriorityCandidate( Map<String, Object> candidates, Class<?> requiredType ) {
		String highestPriorityBeanName = null;
		Integer highestPriority = null;
		for ( Map.Entry<String, Object> entry : candidates.entrySet() ) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if ( beanInstance != null ) {
				Integer candidatePriority = getPriority( beanInstance );
				if ( candidatePriority != null ) {
					if ( highestPriorityBeanName != null ) {
						if ( candidatePriority.equals( highestPriority ) ) {
							throw new NoUniqueBeanDefinitionException( requiredType, candidates.size(),
							                                           "Multiple beans found with the same priority ('" + highestPriority +
									                                           "') among candidates: " + candidates.keySet() );
						}
						else if ( candidatePriority < highestPriority ) {
							highestPriorityBeanName = candidateBeanName;
							highestPriority = candidatePriority;
						}
					}
					else {
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				}
			}
		}
		return highestPriorityBeanName;
	}

	/**
	 * Return whether the bean definition for the given bean name has been
	 * marked as a primary bean.
	 *
	 * @param beanName     the name of the bean
	 * @param beanInstance the corresponding bean instance (can be null)
	 * @return whether the given bean qualifies as primary
	 */
	protected boolean isPrimary( String beanName, Object beanInstance ) {
		if ( containsBeanDefinition( beanName ) ) {
			return getMergedLocalBeanDefinition( beanName ).isPrimary();
		}
		BeanFactory parent = getParentBeanFactory();
		return ( parent instanceof DefaultListableBeanFactory &&
				( (DefaultListableBeanFactory) parent ).isPrimary( beanName, beanInstance ) );
	}

	/**
	 * Return the priority assigned for the given bean instance by
	 * the {@code javax.annotation.Priority} annotation.
	 * <p>The default implementation delegates to the specified
	 * {@link #setDependencyComparator dependency comparator}, checking its
	 * {@link OrderComparator#getPriority method} if it is an extension of
	 * Spring's common {@link OrderComparator} - typically, an
	 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}.
	 * If no such comparator is present, this implementation returns {@code null}.
	 *
	 * @param beanInstance the bean instance to check (can be {@code null})
	 * @return the priority assigned to that bean or {@code null} if none is set
	 */
	@Nullable
	protected Integer getPriority( Object beanInstance ) {
		Comparator<Object> comparator = getDependencyComparator();
		if ( comparator instanceof OrderComparator ) {
			return ( (OrderComparator) comparator ).getPriority( beanInstance );
		}
		return null;
	}

	/**
	 * Determine whether the given candidate name matches the bean name or the aliases
	 * stored in this bean definition.
	 */
	protected boolean matchesBeanName( String beanName, @Nullable String candidateName ) {
		return ( candidateName != null &&
				( candidateName.equals( beanName ) || ObjectUtils.containsElement( getAliases( beanName ), candidateName ) ) );
	}

	/**
	 * Determine whether the given beanName/candidateName pair indicates a self reference,
	 * i.e. whether the candidate points back to the original bean or to a factory method
	 * on the original bean.
	 */
	private boolean isSelfReference( @Nullable String beanName, @Nullable String candidateName ) {
		return ( beanName != null && candidateName != null &&
				( beanName.equals( candidateName ) || ( containsBeanDefinition( candidateName ) &&
						beanName.equals( getMergedLocalBeanDefinition( candidateName ).getFactoryBeanName() ) ) ) );
	}

	/**
	 * Raise a NoSuchBeanDefinitionException or BeanNotOfRequiredTypeException
	 * for an unresolvable dependency.
	 */
	private void raiseNoMatchingBeanFound(
			Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor ) throws BeansException {

		checkBeanNotOfRequiredType( type, descriptor );

		throw new NoSuchBeanDefinitionException( resolvableType,
		                                         "expected at least 1 bean which qualifies as autowire candidate. " +
				                                         "Dependency annotations: " + ObjectUtils.nullSafeToString( descriptor.getAnnotations() ) );
	}

	/**
	 * Raise a BeanNotOfRequiredTypeException for an unresolvable dependency, if applicable,
	 * i.e. if the target type of the bean would match but an exposed proxy doesn't.
	 */
	private void checkBeanNotOfRequiredType( Class<?> type, DependencyDescriptor descriptor ) {
		for ( String beanName : this.beanDefinitionNames ) {
			RootBeanDefinition mbd = getMergedLocalBeanDefinition( beanName );
			Class<?> targetType = mbd.getTargetType();
			if ( targetType != null && type.isAssignableFrom( targetType ) &&
					isAutowireCandidate( beanName, mbd, descriptor, getAutowireCandidateResolver() ) ) {
				// Probably a proxy interfering with target type match -> throw meaningful exception.
				Object beanInstance = getSingleton( beanName, false );
				Class<?> beanType = ( beanInstance != null && beanInstance.getClass() != NullBean.class ?
						beanInstance.getClass() : predictBeanType( beanName, mbd ) );
				if ( beanType != null && !type.isAssignableFrom( beanType ) ) {
					throw new BeanNotOfRequiredTypeException( beanName, type, beanType );
				}
			}
		}

		BeanFactory parent = getParentBeanFactory();
		if ( parent instanceof NewAcrossListableBeanFactory ) {
			( (NewAcrossListableBeanFactory) parent ).checkBeanNotOfRequiredType( type, descriptor );
		}
	}

	/**
	 * Create an {@link Optional} wrapper for the specified dependency.
	 */
	private Optional<?> createOptionalDependency(
			DependencyDescriptor descriptor, @Nullable String beanName, final Object... args ) {

		DependencyDescriptor descriptorToUse = new NewAcrossListableBeanFactory.NestedDependencyDescriptor( descriptor )
		{
			@Override
			public boolean isRequired() {
				return false;
			}

			@Override
			public Object resolveCandidate( String beanName, Class<?> requiredType, BeanFactory beanFactory ) {
				return ( !ObjectUtils.isEmpty( args ) ? beanFactory.getBean( beanName, args ) :
						super.resolveCandidate( beanName, requiredType, beanFactory ) );
			}
		};
		Object result = doResolveDependency( descriptorToUse, beanName, null, null );
		return ( result instanceof Optional ? (Optional<?>) result : Optional.ofNullable( result ) );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( ObjectUtils.identityToString( this ) );
		sb.append( ": defining beans [" );
		sb.append( StringUtils.collectionToCommaDelimitedString( this.beanDefinitionNames ) );
		sb.append( "]; " );
		BeanFactory parent = getParentBeanFactory();
		if ( parent == null ) {
			sb.append( "root of factory hierarchy" );
		}
		else {
			sb.append( "parent: " ).append( ObjectUtils.identityToString( parent ) );
		}
		return sb.toString();
	}

	//---------------------------------------------------------------------
	// Across specific methods
	//---------------------------------------------------------------------

	@Override
	public void setParentBeanFactory( BeanFactory parentBeanFactory ) {
		this.parentBeanFactory = parentBeanFactory;
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}

	/**
	 * Check if a bean with a given name is an exposed bean.
	 */
	public boolean isExposedBean( String beanName ) {
		return beanDefinitionMap.get( beanName ) instanceof ExposedBeanDefinition;
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

	/**
	 * Retrieve an {@link AcrossOrderSpecifier} for a local bean or singleton.
	 * If neither is present with that name, {@code null} will be returned.
	 *
	 * @param beanName bean name
	 * @return order specifier
	 */
	@Nullable
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

	@Override
	protected RootBeanDefinition getMergedBeanDefinition( String beanName,
	                                                      BeanDefinition bd,
	                                                      BeanDefinition containingBd ) throws BeanDefinitionStoreException {
		if ( bd instanceof ExposedBeanDefinition ) {
			return (ExposedBeanDefinition) bd;
		}
		return super.getMergedBeanDefinition( beanName, bd, containingBd );
	}

	// @Override
	// protected Object getSingleton( String beanName, boolean allowEarlyReference ) {
	// 	BeanDefinition beanDefinition = beanDefinitionMap.get( beanName );
	// 	if ( beanDefinition instanceof ExposedBeanDefinition ) {
	// 		return getExposedBean( (ExposedBeanDefinition) beanDefinition );
	// 	}
	// 	return super.getSingleton( beanName, allowEarlyReference );
	// }
	//
	// @Override
	// public Object getSingleton( String beanName, ObjectFactory<?> singletonFactory ) {
	// 	BeanDefinition beanDefinition = beanDefinitionMap.get( beanName );
	// 	if ( beanDefinition instanceof ExposedBeanDefinition ) {
	// 		return getExposedBean( (ExposedBeanDefinition) beanDefinition );
	// 	}
	// 	return super.getSingleton( beanName, singletonFactory );
	// }

	@Override
	protected Object doCreateBean( String beanName, RootBeanDefinition mbd, Object[] args ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			getExposedBean( (ExposedBeanDefinition) mbd );
		}

		return super.doCreateBean( beanName, mbd, args );
	}

	@Override
	protected <T> T doGetBean( String name, Class<T> requiredType, Object[] args, boolean typeCheckOnly ) throws BeansException {
		String beanName = BeanFactoryUtils.transformedBeanName( name );

		BeanDefinition beanDefinition = beanDefinitionMap.get( beanName );
		if ( beanDefinition instanceof ExposedBeanDefinition ) {
			// todo: check
			ExposedBeanDefinition mbd = (ExposedBeanDefinition) getBeanDefinition( beanName );
			AcrossContextInfo contextInfo = acrossContextBeanRegistry( mbd.getFactoryBeanName() ).getContextInfo();
			NewAcrossListableBeanFactory moduleBeanFactory =
					(NewAcrossListableBeanFactory) ( mbd.getModuleName() != null
							? contextInfo.getModuleInfo( mbd.getModuleName() ).getApplicationContext().getAutowireCapableBeanFactory()
							: contextInfo.getApplicationContext().getAutowireCapableBeanFactory() );

			String originalBeanName = isFactoryDereference( name ) ? FACTORY_BEAN_PREFIX + mbd.getOriginalBeanName() : mbd.getOriginalBeanName();
			return moduleBeanFactory.doGetBean( originalBeanName, requiredType, args, typeCheckOnly );
		}

		return super.doGetBean( name, requiredType, args, typeCheckOnly );
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
				NewAcrossListableBeanFactory moduleBeanFactory =
						(NewAcrossListableBeanFactory) ( ebd.getModuleName() != null
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
				NewAcrossListableBeanFactory moduleBeanFactory =
						(NewAcrossListableBeanFactory) ( mbd.getModuleName() != null
								? contextInfo.getModuleInfo( mbd.getModuleName() ).getApplicationContext().getAutowireCapableBeanFactory()
								: contextInfo.getApplicationContext().getAutowireCapableBeanFactory() );
				RootBeanDefinition originalBd = moduleBeanFactory.getMergedLocalBeanDefinition( mbd.getOriginalBeanName() );

				return moduleBeanFactory.isFactoryBean( mbd.getOriginalBeanName(), originalBd )
						&& moduleBeanFactory.isTypeMatch( FACTORY_BEAN_PREFIX + mbd.getOriginalBeanName(), typeToMatch );
			}
		}

		return super.isTypeMatch( name, typeToMatch );
	}

	@Override
	protected boolean requiresDestruction( Object bean, RootBeanDefinition mbd ) {
		if ( mbd instanceof ExposedBeanDefinition ) {
			// exposed beans should be destroyed in their original bean factory
			return false;
		}
		return super.requiresDestruction( bean, mbd );
	}

	private Object getExposedBean( ExposedBeanDefinition beanDefinition ) {
		List<ConstructorArgumentValues.ValueHolder> factoryArguments = beanDefinition.getConstructorArgumentValues().getGenericArgumentValues();

		return acrossContextBeanRegistry( beanDefinition.getFactoryBeanName() ).getBeanFromModule(
				(String) factoryArguments.get( 0 ).getValue(),
				(String) factoryArguments.get( 1 ).getValue()
		);
	}

	private AcrossContextBeanRegistry acrossContextBeanRegistry( String beanName ) {
		return (AcrossContextBeanRegistry) getBean( beanName );
	}

	private boolean isInLocalBeansOperationMode() {
		Map<Object, Boolean> beanFactoryMap = localBeansOperationMode.get();
		return beanFactoryMap != null && Boolean.TRUE.equals( beanFactoryMap.get( this ) );
	}

	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
		throw new NotSerializableException( "DefaultListableBeanFactory itself is not deserializable - " +
				                                    "just a SerializedBeanFactoryReference is" );
	}

	protected Object writeReplace() throws ObjectStreamException {
		if ( this.serializationId != null ) {
			return new NewAcrossListableBeanFactory.SerializedBeanFactoryReference( this.serializationId );
		}
		else {
			throw new NotSerializableException( "DefaultListableBeanFactory has no serialization id" );
		}
	}

	/**
	 * Minimal id reference to the factory.
	 * Resolved to the actual factory instance on deserialization.
	 */
	private static class SerializedBeanFactoryReference implements Serializable
	{

		private final String id;

		public SerializedBeanFactoryReference( String id ) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get( this.id );
			if ( ref != null ) {
				Object result = ref.get();
				if ( result != null ) {
					return result;
				}
			}
			// Lenient fallback: dummy factory in case of original factory not found...
			NewAcrossListableBeanFactory dummyFactory = new AcrossListableBeanFactory();
			dummyFactory.serializationId = this.id;
			return dummyFactory;
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
			beanFactoryMap.put( NewAcrossListableBeanFactory.this, true );

		}

		@Override
		public void close() {
			Map<Object, Boolean> beanFactoryMap = localBeansOperationMode.get();
			if ( beanFactoryMap != null ) {
				beanFactoryMap.remove( NewAcrossListableBeanFactory.this );
				if ( beanFactoryMap.isEmpty() ) {
					localBeansOperationMode.remove();
				}
			}
		}
	}

	/**
	 * A dependency descriptor marker for nested elements.
	 */
	private static class NestedDependencyDescriptor extends DependencyDescriptor
	{

		public NestedDependencyDescriptor( DependencyDescriptor original ) {
			super( original );
			increaseNestingLevel();
		}
	}

	/**
	 * A dependency descriptor for a multi-element declaration with nested elements.
	 */
	private static class MultiElementDescriptor extends NewAcrossListableBeanFactory.NestedDependencyDescriptor
	{

		public MultiElementDescriptor( DependencyDescriptor original ) {
			super( original );
		}
	}

	/**
	 * A dependency descriptor marker for stream access to multiple elements.
	 */
	private static class StreamDependencyDescriptor extends DependencyDescriptor
	{

		private final boolean ordered;

		public StreamDependencyDescriptor( DependencyDescriptor original, boolean ordered ) {
			super( original );
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}

	private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable
	{
	}

	/**
	 * Serializable ObjectFactory/ObjectProvider for lazy resolution of a dependency.
	 */
	private class DependencyObjectProvider implements NewAcrossListableBeanFactory.BeanObjectProvider<Object>
	{

		private final DependencyDescriptor descriptor;

		private final boolean optional;

		@Nullable
		private final String beanName;

		public DependencyObjectProvider( DependencyDescriptor descriptor, @Nullable String beanName ) {
			this.descriptor = new NewAcrossListableBeanFactory.NestedDependencyDescriptor( descriptor );
			this.optional = ( this.descriptor.getDependencyType() == Optional.class );
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			if ( this.optional ) {
				return createOptionalDependency( this.descriptor, this.beanName );
			}
			else {
				Object result = doResolveDependency( this.descriptor, this.beanName, null, null );
				if ( result == null ) {
					throw new NoSuchBeanDefinitionException( this.descriptor.getResolvableType() );
				}
				return result;
			}
		}

		@Override
		public Object getObject( final Object... args ) throws BeansException {
			if ( this.optional ) {
				return createOptionalDependency( this.descriptor, this.beanName, args );
			}
			else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor( this.descriptor )
				{
					@Override
					public Object resolveCandidate( String beanName, Class<?> requiredType, BeanFactory beanFactory ) {
						return beanFactory.getBean( beanName, args );
					}
				};
				Object result = doResolveDependency( descriptorToUse, this.beanName, null, null );
				if ( result == null ) {
					throw new NoSuchBeanDefinitionException( this.descriptor.getResolvableType() );
				}
				return result;
			}
		}

		@Override
		@Nullable
		public Object getIfAvailable() throws BeansException {
			if ( this.optional ) {
				return createOptionalDependency( this.descriptor, this.beanName );
			}
			else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor( this.descriptor )
				{
					@Override
					public boolean isRequired() {
						return false;
					}
				};
				return doResolveDependency( descriptorToUse, this.beanName, null, null );
			}
		}

		@Override
		@Nullable
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor( this.descriptor )
			{
				@Override
				public boolean isRequired() {
					return false;
				}

				@Override
				@Nullable
				public Object resolveNotUnique( ResolvableType type, Map<String, Object> matchingBeans ) {
					return null;
				}
			};
			if ( this.optional ) {
				return createOptionalDependency( descriptorToUse, this.beanName );
			}
			else {
				return doResolveDependency( descriptorToUse, this.beanName, null, null );
			}
		}

		@Nullable
		protected Object getValue() throws BeansException {
			if ( this.optional ) {
				return createOptionalDependency( this.descriptor, this.beanName );
			}
			else {
				return doResolveDependency( this.descriptor, this.beanName, null, null );
			}
		}

		@Override
		public Stream<Object> stream() {
			return resolveStream( false );
		}

		@Override
		public Stream<Object> orderedStream() {
			return resolveStream( true );
		}

		@SuppressWarnings("unchecked")
		private Stream<Object> resolveStream( boolean ordered ) {
			DependencyDescriptor descriptorToUse = new NewAcrossListableBeanFactory.StreamDependencyDescriptor( this.descriptor, ordered );
			Object result = doResolveDependency( descriptorToUse, this.beanName, null, null );
			return ( result instanceof Stream ? (Stream<Object>) result : Stream.of( result ) );
		}
	}

	/**
	 * Separate inner class for avoiding a hard dependency on the {@code javax.inject} API.
	 * Actual {@code javax.inject.Provider} implementation is nested here in order to make it
	 * invisible for Graal's introspection of DefaultListableBeanFactory's nested classes.
	 */
	private class Jsr330Factory implements Serializable
	{

		public Object createDependencyProvider( DependencyDescriptor descriptor, @Nullable String beanName ) {
			return new NewAcrossListableBeanFactory.Jsr330Factory.Jsr330Provider( descriptor, beanName );
		}

		private class Jsr330Provider extends NewAcrossListableBeanFactory.DependencyObjectProvider implements Provider<Object>
		{

			public Jsr330Provider( DependencyDescriptor descriptor, @Nullable String beanName ) {
				super( descriptor, beanName );
			}

			@Override
			@Nullable
			public Object get() throws BeansException {
				return getValue();
			}
		}
	}

	/**
	 * An {@link org.springframework.core.OrderComparator.OrderSourceProvider} implementation
	 * that is aware of the bean metadata of the instances to sort.
	 * <p>Lookup for the method factory of an instance to sort, if any, and let the
	 * comparator retrieve the {@link org.springframework.core.annotation.Order}
	 * value defined on it. This essentially allows for the following construct:
	 */
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider
	{

		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider( Map<Object, String> instancesToBeanNames ) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		@Nullable
		public Object getOrderSource( Object obj ) {
			String beanName = this.instancesToBeanNames.get( obj );
			if ( beanName == null || !containsBeanDefinition( beanName ) ) {
				return null;
			}
			RootBeanDefinition beanDefinition = getMergedLocalBeanDefinition( beanName );
			List<Object> sources = new ArrayList<>( 2 );
			Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
			if ( factoryMethod != null ) {
				sources.add( factoryMethod );
			}
			Class<?> targetType = beanDefinition.getTargetType();
			if ( targetType != null && targetType != obj.getClass() ) {
				sources.add( targetType );
			}
			return sources.toArray();
		}
	}
}