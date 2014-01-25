package com.foreach.across.core.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.events.AcrossEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Helper methods for AcrossContext configuration.
 */
public final class AcrossContextUtil
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContextUtil.class );

	private AcrossContextUtil() {
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
			ApplicationContext moduleContext = AcrossContextUtil.getApplicationContext( module );
			ConfigurableListableBeanFactory beanFactory = AcrossContextUtil.getBeanFactory( module );

			Collection<Object> refreshableBeans =
					ApplicationContextScanner.findBeansWithAnnotation( moduleContext, Refreshable.class );

			for ( Object singleton : refreshableBeans ) {
				beanFactory.autowireBeanProperties( singleton, AutowireCapableBeanFactory.AUTOWIRE_NO, false );

				Class beanClass = singleton.getClass();

				for ( Method method : ReflectionUtils.getUniqueDeclaredMethods( beanClass ) ) {
					if ( AnnotationUtils.getAnnotation( method, PostRefresh.class ) != null ) {

						if ( method.getParameterTypes().length != 0 ) {
							LOG.error( "@PostRefresh method {} should be parameter-less", method );
						}
						else {
							try {
								method.setAccessible( true );
								method.invoke( singleton );
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
	 * @return ApplicationContext defined in the holder.
	 */
	public static AbstractApplicationContext getApplicationContext( AcrossApplicationContextHolder contextOrModule ) {
		return contextOrModule.getAcrossApplicationContext().getApplicationContext();
	}

	/**
	 * Returns the Spring ApplicationContext that this AcrossContext is a child of.
	 *
	 * @param context AcrossContext instance.
	 * @return ApplicationContext that is the parent.
	 */
	public static ApplicationContext getParentApplicationContext( AcrossContext context ) {
		return context.getParentApplicationContext();
	}

	/**
	 * Returns the Spring BeanFactory associated with the given AcrossContext or AcrossModule.
	 *
	 * @param contextOrModule AcrossApplicationHolder instance.
	 * @return BeanFactory linked to the ApplicationContext in the holder.
	 */
	public static ConfigurableListableBeanFactory getBeanFactory( AcrossApplicationContextHolder contextOrModule ) {
		return contextOrModule.getAcrossApplicationContext().getBeanFactory();
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
}
