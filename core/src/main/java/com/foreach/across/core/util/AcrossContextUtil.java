package com.foreach.across.core.util;

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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;

public class AcrossContextUtil
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossContextUtil.class );

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
			ApplicationContext moduleContext = module.getApplicationContext();
			ConfigurableListableBeanFactory beanFactory =
					(ConfigurableListableBeanFactory) moduleContext.getAutowireCapableBeanFactory();

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

}
