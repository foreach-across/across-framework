package com.foreach.across.core.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.events.AcrossEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Helper methods for AcrossContext configuration.
 */
public final class AcrossContextUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContextUtils.class );

	private AcrossContextUtils() {
	}

	/**
	 * Scans for all AcrossEventHandler instances inside the context specified, and will
	 * register them with the AcrossEventPublisher.
	 */
	public static void autoRegisterEventHandlers( ApplicationContext applicationContext,
	                                              AcrossEventPublisher publisher ) {
		Collection<Object> handlers =
				ApplicationContextScanner.findBeansWithAnnotation( applicationContext, AcrossEventHandler.class );

		for ( Object handler : handlers ) {
			publisher.subscribe( handler );
		}
	}

	/**
	 * Will refresh all @Refreshable annotated components in the AcrossContext.
	 * This performes the annotated autowiring again and calls the optional @PostRefresh method.
	 */
	public static void refreshBeans( AcrossContext context ) {
		for ( AcrossModule module : context.getModules() ) {
			ApplicationContext moduleContext = AcrossContextUtils.getApplicationContext( module );
			ConfigurableListableBeanFactory beanFactory = AcrossContextUtils.getBeanFactory( module );

			Collection<Object> refreshableBeans =
					ApplicationContextScanner.findBeansWithAnnotation( moduleContext, Refreshable.class );

			for ( Object singleton : refreshableBeans ) {
				Object bean = AcrossContextUtils.getProxyTarget( singleton );
				beanFactory.autowireBeanProperties( bean, AutowireCapableBeanFactory.AUTOWIRE_NO, false );

				Class beanClass = ClassUtils.getUserClass( AopProxyUtils.ultimateTargetClass( singleton ) );

				for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( beanClass ) ) {
					if ( AnnotationUtils.getAnnotation( method, PostRefresh.class ) != null ) {

						if ( method.getParameterTypes().length != 0 ) {
							LOG.error( "@PostRefresh method {} should be parameter-less", method );
						}
						else {
							try {
								method.setAccessible( true );
								method.invoke( bean );
							}
							catch ( Exception e ) {
								LOG.error( "Exception executing @PostRefresh method", e );
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the Spring ApplicationContext associated with the given AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return ApplicationContext defined in the holder or null if none.
	 */
	public static AbstractApplicationContext getApplicationContext( AcrossApplicationContextHolder contextOrModule ) {
		return contextOrModule.hasApplicationContext() ? contextOrModule.getAcrossApplicationContext().getApplicationContext() : null;
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
	public static ConfigurableListableBeanFactory getBeanFactory( AcrossApplicationContextHolder contextOrModule ) {
		return contextOrModule.hasApplicationContext() ? contextOrModule.getAcrossApplicationContext().getBeanFactory() : null;
	}

	/**
	 * Returns the Spring ApplicationContext associated with the given AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return AcrossApplicationContext wrapping the Spring ApplicationContext.
	 */
	public static AcrossApplicationContext getAcrossApplicationContext( AcrossApplicationContextHolder contextOrModule ) {
		return contextOrModule.getAcrossApplicationContext();
	}

	/**
	 * Sets the ApplicationContext wrapper on an AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule    AcrossApplicationHolder instance.
	 * @param applicationContext AcrossApplicationContext instance.
	 */
	public static void setAcrossApplicationContext( AcrossApplicationContextHolder contextOrModule,
	                                                AcrossApplicationContext applicationContext ) {
		contextOrModule.setAcrossApplicationContext( applicationContext );
	}

	/**
	 * Searches the specified context for a bean of the given type.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @param requiredType    Type the bean should match.
	 * @param <T>             Type of the matching bean.
	 * @return Bean found.  Exception is thrown if none is found.
	 */
	public static <T> T getBeanOfType( AcrossApplicationContextHolder contextOrModule, Class<T> requiredType ) {
		return contextOrModule.getAcrossApplicationContext().getBeanFactory().getBean( requiredType );
	}

	/**
	 * Searches the AcrossContext and its parent for beans of the given type.  Will only include exposed beans.
	 *
	 * @param context      AcrossContext instance.
	 * @param requiredType Type the bean should match.
	 * @param <T>          Type of the matching beans.
	 */
	public static <T> Collection<T> getBeansOfType( AcrossContext context, Class<T> requiredType ) {
		return getBeansOfType( context, requiredType, false );
	}

	/**
	 * Searches the AcrossContext for beans of the given type.  Depending on the scanModules boolean, this
	 * will scan the base context and its parent, or all modules separately (including non-exposed beans).
	 *
	 * @param context      AcrossContext instance.
	 * @param requiredType Type the bean should match.
	 * @param scanModules  True if the individual AcrossModules should be scanned.
	 * @param <T>          Type of the matching beans.
	 */
	public static <T> Collection<T> getBeansOfType( AcrossContext context,
	                                                Class<T> requiredType,
	                                                boolean scanModules ) {
		Set<T> beans = new HashSet<T>();
		beans.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors( getBeanFactory( context ), requiredType ).values() );

		if ( scanModules ) {
			for ( AcrossModule module : context.getModules() ) {
				ListableBeanFactory beanFactory = getBeanFactory( module );

				if ( beanFactory != null ) {
					beans.addAll( beanFactory.getBeansOfType( requiredType ).values() );
				}
			}
		}

		return beans;
	}

	/**
	 * Will list all ApplicationContextConfigurers in the module, combined with the ones registered on the
	 * AcrossContext that are specified to apply to all modules.
	 *
	 * @param context AcrossContext instance.
	 * @param module  AcrossModule instance.
	 * @return Merged set of ApplicationContextConfigurers.
	 */
	public static Collection<ApplicationContextConfigurer> getConfigurersToApply( AcrossContext context,
	                                                                              AcrossModule module ) {
		Set<ApplicationContextConfigurer> configurers = new LinkedHashSet<ApplicationContextConfigurer>();
		configurers.addAll( module.getApplicationContextConfigurers() );

		for ( Map.Entry<ApplicationContextConfigurer, Boolean> configurerEntry : context.getApplicationContextConfigurers().entrySet() ) {
			if ( configurerEntry.getValue() ) {
				configurers.add( configurerEntry.getKey() );
			}
		}

		return configurers;
	}

	/**
	 * Unwraps the target from a proxy (or multiple proxy) hierarchy.
	 *
	 * @param instance Bean that can be proxied or not.
	 * @return Bean itself or final target of a set of proxies.
	 */
	public static Object getProxyTarget( Object instance ) {
		try {
			if ( AopUtils.isJdkDynamicProxy( instance ) ) {
				return getProxyTarget( ( (Advised) instance ).getTargetSource().getTarget() );
			}
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}

		return instance;
	}
}
