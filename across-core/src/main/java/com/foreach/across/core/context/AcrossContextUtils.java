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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossModuleUtils;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.config.AcrossConfig;
import com.foreach.across.core.config.AcrossInstallerConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.core.context.configurer.PropertySourcesConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.filters.AnnotatedMethodFilter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.aop.target.SimpleBeanTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.AliasRegistry;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

import static com.foreach.across.core.AcrossContext.DATASOURCE;
import static com.foreach.across.core.AcrossContext.INSTALLER_DATASOURCE;

/**
 * Helper methods for AcrossContext configuration.
 * Only to be used internal in the framework or
 */
public final class AcrossContextUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContextUtils.class );

	private AcrossContextUtils() {
	}

	/**
	 * Will refresh all @Refreshable annotated components in the AcrossContext and perform annotated
	 * autowiring again.  Additionally will scan for all @PostRefresh methods and execute those.
	 */
	public static void refreshBeans( AcrossContext context ) {
		AcrossContextInfo contextInfo = getContextInfo( context );

		if ( contextInfo != null ) {
			for ( AcrossModuleInfo moduleInfo : contextInfo.getModules() ) {
				if ( !moduleInfo.isBootstrapped() ) {
					continue;
				}

				ApplicationContext moduleContext = moduleInfo.getApplicationContext();
				ConfigurableListableBeanFactory beanFactory = AcrossModuleUtils.beanFactory( moduleInfo );

				Collection<Object> refreshableBeans = ApplicationContextScanner.findBeansWithAnnotation( moduleContext, Refreshable.class );

				for ( Object singleton : refreshableBeans ) {
					Object bean = AcrossContextUtils.getProxyTarget( singleton );
					if ( bean != null ) {
						beanFactory.autowireBeanProperties( bean, AutowireCapableBeanFactory.AUTOWIRE_NO, false );
					}
				}

				Map<String, Object> postRefreshBeans = ApplicationContextScanner.findSingletonsMatching(
						moduleContext,
						new AnnotatedMethodFilter( PostRefresh.class ) );

				postRefreshBeans.forEach( ( beanName, singleton ) -> {
					Object bean = AcrossContextUtils.getProxyTarget( singleton );
					if ( bean != null ) {
						Class beanClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( singleton ) );

						for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( beanClass ) ) {
							PostRefresh postRefresh = AnnotationUtils.getAnnotation( method, PostRefresh.class );
							if ( postRefresh != null ) {
								boolean required = postRefresh.required();
								Class<?>[] paramTypes = method.getParameterTypes();
								Object[] arguments = new Object[paramTypes.length];
								Set<String> autowiredBeans = new LinkedHashSet<>( paramTypes.length );
								TypeConverter typeConverter = beanFactory.getTypeConverter();

								for ( int i = 0; i < arguments.length; i++ ) {
									MethodParameter methodParam = new MethodParameter( method, i );
									DependencyDescriptor currDesc = new DependencyDescriptor( methodParam, required );
									currDesc.setContainingClass( bean.getClass() );

									try {
										Object arg = beanFactory.resolveDependency( currDesc, beanName, autowiredBeans, typeConverter );

										if ( arg == null && !required ) {
											arguments = null;
											break;
										}

										arguments[i] = arg;
									}
									catch ( BeansException ex ) {
										throw new UnsatisfiedDependencyException( null, beanName, new InjectionPoint( methodParam ), ex );
									}
								}

								if ( arguments != null ) {
									try {
										ReflectionUtils.makeAccessible( method );
										method.invoke( bean, arguments );
									}
									catch ( RuntimeException rte ) {
										throw rte;
									}
									catch ( Exception ex ) {
										throw new RuntimeException( ex );
									}
								}
							}
						}
					}
				} );
			}
		}
	}

	/**
	 * Returns the Spring ApplicationContext associated with the given AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return ApplicationContext defined in the holder or null if none.
	 */
	public static AcrossConfigurableApplicationContext getApplicationContext( AcrossEntity contextOrModule ) {
		if ( contextOrModule instanceof AcrossModuleInfo ) {
			return getApplicationContext( ( (AcrossModuleInfo) contextOrModule ).getModule() );
		}
		if ( contextOrModule instanceof AcrossContextInfo ) {
			return getApplicationContext( ( (AcrossContextInfo) contextOrModule ).getContext() );
		}

		AbstractAcrossEntity aEntity = (AbstractAcrossEntity) contextOrModule;
		if ( aEntity.hasApplicationContext() ) {
			return aEntity.getAcrossApplicationContextHolder().getApplicationContext();
		}
		throw new IllegalStateException( "No ApplicationContext is available - has the Across context or module been bootstrapped?" );
	}

	/**
	 * Returns the Spring ApplicationContext that this AcrossContext is a child of.
	 *
	 * @param context AcrossContext instance.
	 * @return ApplicationContext that is the parent.
	 */
	public static ApplicationContext getParentApplicationContext( AcrossContext context ) {
		return getApplicationContext( context ).getParent();
	}

	/**
	 * Returns the Spring BeanFactory associated with the given AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return BeanFactory linked to the ApplicationContext in the holder or null if not yet available.
	 */
	public static AcrossListableBeanFactory getBeanFactory( AcrossEntity contextOrModule ) {
		return getAcrossApplicationContextHolder( contextOrModule ).getBeanFactory();
	}

	/**
	 * Returns the running AcrossContextInfo for a defined AcrossContext.
	 *
	 * @param context AcrossContext instance.
	 * @return AcrossContextInfo of the running context (null if none).
	 */
	public static AcrossContextInfo getContextInfo( AcrossContext context ) {
		ApplicationContext applicationContext = getApplicationContext( context );

		return applicationContext != null ? applicationContext.getBean( AcrossContextInfo.class ) : null;
	}

	/**
	 * Returns the created BeanRegistry for a configured Across entity.
	 *
	 * @param acrossEntity AcrossContext/AcrossModule or AcrossContextInfo/AcrossModuleInfo instance
	 * @return AcrossContextBeanRegistry of the running context (null if none).
	 */
	public static AcrossContextBeanRegistry getBeanRegistry( AcrossEntity acrossEntity ) {
		ApplicationContext applicationContext = getApplicationContext( acrossEntity );

		return applicationContext != null ? applicationContext.getBean( AcrossContextBeanRegistry.class ) : null;
	}

	/**
	 * Sets the ApplicationContext wrapper on an AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule    AbstractAcrossEntity instance.
	 * @param applicationContext AcrossApplicationContext instance.
	 */
	public static void setAcrossApplicationContextHolder( AbstractAcrossEntity contextOrModule,
	                                                      AcrossApplicationContextHolder applicationContext ) {
		contextOrModule.setAcrossApplicationContextHolder( applicationContext );
	}

	/**
	 * Returns the AcrossApplicationContext attached to the module or context entity.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return Across application context information.
	 */
	public static AcrossApplicationContextHolder getAcrossApplicationContextHolder( AcrossEntity contextOrModule ) {
		if ( contextOrModule instanceof AbstractAcrossEntity ) {
			return ( (AbstractAcrossEntity) contextOrModule ).getAcrossApplicationContextHolder();
		}
		else if ( contextOrModule instanceof AcrossModuleInfo ) {
			return ( (AcrossModuleInfo) contextOrModule ).getModule().getAcrossApplicationContextHolder();
		}
		else if ( contextOrModule instanceof AcrossContextInfo ) {
			return ( (AcrossContextInfo) contextOrModule ).getContext().getAcrossApplicationContextHolder();
		}

		return null;
	}

	/**
	 * Will list all ApplicationContextConfigurers to apply for the AcrossContext itself.
	 *
	 * @param context AcrossContext instance.
	 * @return Merges set of ApplicationContextConfigurers.
	 */
	public static Collection<ApplicationContextConfigurer> getApplicationContextConfigurers( AcrossContext context ) {
		Set<ApplicationContextConfigurer> configurers = new LinkedHashSet<>();
		configurers.add( new AnnotatedClassConfigurer( AcrossConfig.class ) );

		configurers.add( new AnnotatedClassConfigurer( AcrossInstallerConfig.class ) );

		for ( Map.Entry<ApplicationContextConfigurer, ConfigurerScope> configurerEntry : context
				.getApplicationContextConfigurers().entrySet() ) {
			if ( configurerEntry.getValue() != ConfigurerScope.MODULES_ONLY ) {
				configurers.add( configurerEntry.getKey() );
			}
		}

		// If properties are set on the context, add them last
		Properties contextProperties = context.getProperties();

		if ( !contextProperties.isEmpty() ) {
			configurers.add( new PropertySourcesConfigurer(
					new PropertiesPropertySource( AcrossContext.BEAN, contextProperties ) ) );
		}

		return configurers;
	}

	/**
	 * Will list all ApplicationContextConfigurers in the module, combined with the ones registered on the
	 * AcrossContext that are specified to apply to all modules.
	 *
	 * @param context AcrossContext instance.
	 * @param module  AcrossModule instance.
	 * @return Merged set of ApplicationContextConfigurers.
	 */
	public static Collection<ApplicationContextConfigurer> getApplicationContextConfigurers(
			AcrossContext context, AcrossModule module
	) {
		Set<ApplicationContextConfigurer> configurers = new LinkedHashSet<>();

		// First add configurers defined on the context
		for ( Map.Entry<ApplicationContextConfigurer, ConfigurerScope> configurerEntry : context
				.getApplicationContextConfigurers().entrySet() ) {
			if ( configurerEntry.getValue() != ConfigurerScope.CONTEXT_ONLY ) {
				configurers.add( configurerEntry.getKey() );
			}
		}

		// Add module defined configurers
		configurers.addAll( module.getApplicationContextConfigurers() );

		// Finally add properties set on the module
		Properties moduleProperties = module.getProperties();

		if ( !moduleProperties.isEmpty() ) {
			configurers.add( new PropertySourcesConfigurer(
					new PropertiesPropertySource( module.getName(), moduleProperties )
			) );
		}

		return configurers;
	}

	/**
	 * Will list all ApplicationContextConfigurers to be added to an installer context for the module, along
	 * with all properties that should be registered.
	 *
	 * @param module AcrossModule instance.
	 * @return Merged set of ApplicationContextConfigurers.
	 */
	public static Collection<ApplicationContextConfigurer> getInstallerContextConfigurers( AcrossModule module ) {
		Set<ApplicationContextConfigurer> configurers = new LinkedHashSet<>();

		// Add module defined configurers
		configurers.addAll( module.getInstallerContextConfigurers() );

		// Finally add properties set on the module
		Properties moduleProperties = module.getProperties();

		if ( !moduleProperties.isEmpty() ) {
			configurers.add( new PropertySourcesConfigurer(
					new PropertiesPropertySource( module.getName(), moduleProperties )
			) );
		}

		return configurers;
	}

	/**
	 * Unwraps the target from a proxy (or multiple proxy) hierarchy.
	 * If the proxy is a {@link AbstractLazyCreationTargetSource} then the target bean will only be
	 * returned if it has been initialized.  We do not want calls to this method to force any kind
	 * of initialization of the target.
	 * <p/>
	 * If the proxy is a {@link org.springframework.aop.target.SimpleBeanTargetSource}, then the target
	 * bean name will be examined and if it is a scopedTarget, it will not be returned either.
	 * <p/>
	 * Meant for internal use in the Across framework.
	 *
	 * @param instance Bean that can be proxied or not.
	 * @return Bean itself or final target of a set of proxies.
	 */
	@SneakyThrows
	public static Object getProxyTarget( Object instance ) {
		if ( AopUtils.isJdkDynamicProxy( instance ) ) {
			TargetSource targetSource = ( (Advised) instance ).getTargetSource();

			if ( targetSource instanceof AbstractLazyCreationTargetSource ) {
				AbstractLazyCreationTargetSource lazyCreationTargetSource
						= (AbstractLazyCreationTargetSource) targetSource;

				if ( lazyCreationTargetSource.isInitialized() ) {
					return getProxyTarget( lazyCreationTargetSource.getTarget() );
				}

				LOG.trace( "Attempt to retrieve uninitialized proxy target: {}",
				           lazyCreationTargetSource.getTargetClass() );
				return null;
			}
			else if ( targetSource instanceof LazyInitTargetSource ) {
				LOG.trace( "Skipping LazyInitTargetSource - unable to access proxy target without possible throwing exception" );
				return null;
			}
			else if ( targetSource instanceof SimpleBeanTargetSource ) {
				SimpleBeanTargetSource beanTargetSource = (SimpleBeanTargetSource) targetSource;
				String targetBeanName = beanTargetSource.getTargetBeanName();

				if ( StringUtils.isEmpty( targetBeanName )
						|| StringUtils.startsWith( targetBeanName, "scopedTarget." ) ) {
					LOG.trace( "Refusing proxy target for a scoped target bean, might not be initialized." );
					return null;
				}
			}

			return getProxyTarget( targetSource.getTarget() );
		}

		return instance;
	}

	/***
	 * Since Spring 5.2, {@link DefaultListableBeanFactory} will cache mergedBeanDefinitionHolders.
	 *
	 * After registering an alias, it is required to remove this cached item.
	 *
	 * Since {@link DefaultListableBeanFactory#clearMergedBeanDefinition(String)} is protected, we can only flush all caches.
	 */
	public static void registerBeanDefinitionAlias( AliasRegistry registry, String name, String alias ) {
		if ( registry != null ) {
			registry.registerAlias( name, alias );
			if( registry instanceof AbstractBeanFactory ) {
				// getAliases() would fail because this gets cached before the registrar is called
				(( AbstractBeanFactory) registry).clearMetadataCache();
			}
		}
	}
}
