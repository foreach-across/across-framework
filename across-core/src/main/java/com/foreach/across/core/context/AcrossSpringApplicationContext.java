package com.foreach.across.core.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

/**
 * ApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossSpringApplicationContext extends AnnotationConfigApplicationContext
{
	private Map<String, Object> providedSingletons;

	/**
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AcrossSpringApplicationContext( Map<String, Object> providedSingletons ) {
		this.providedSingletons = providedSingletons;
	}

	/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 *
	 * @param beanFactory the BeanFactory to configure
	 */
	@Override
	protected void prepareBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
		super.prepareBeanFactory( beanFactory );

		// Register additional singletons
		if ( providedSingletons != null ) {
			for ( Map.Entry<String, Object> singleton : providedSingletons.entrySet() ) {
				beanFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			}
		}
	}
}
