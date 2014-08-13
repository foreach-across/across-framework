package com.foreach.across.modules.web.context;

import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.beans.ProvidedBeansMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * WebApplicationContext that allows a set of preregistered singletons to be passed in.
 */
public class AcrossWebApplicationContext extends AnnotationConfigWebApplicationContext implements AcrossConfigurableApplicationContext
{
	private Collection<ProvidedBeansMap> providedBeansMaps = new LinkedHashSet<ProvidedBeansMap>();

	@Override
	protected DefaultListableBeanFactory createBeanFactory() {
		return new AcrossListableBeanFactory( getInternalParentBeanFactory() );
	}

	/**
	 * Adds a collection of provided beans to application context.
	 *
	 * @param beans One or more ProvidedBeansMaps to add.
	 */
	public void provide( ProvidedBeansMap... beans ) {
		for ( ProvidedBeansMap map : beans ) {
			if ( map != null ) {
				providedBeansMaps.add( map );
			}
		}
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

		DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;

		for ( ProvidedBeansMap providedBeans : providedBeansMaps ) {
			for ( Map.Entry<String, BeanDefinition> definition : providedBeans.getBeanDefinitions().entrySet() ) {
				listableBeanFactory.registerBeanDefinition( definition.getKey(), definition.getValue() );
			}
			for ( Map.Entry<String, Object> singleton : providedBeans.getSingletons().entrySet() ) {
				listableBeanFactory.registerSingleton( singleton.getKey(), singleton.getValue() );
			}
		}
	}
}
