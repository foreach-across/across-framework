package com.foreach.across.modules.web.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Map;

/**
 * WebApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossSpringWebApplicationContext extends AnnotationConfigWebApplicationContext
{
	private Map<String, Object> providedSingletons;

	public AcrossSpringWebApplicationContext( Map<String, Object> providedSingletons ) {
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
