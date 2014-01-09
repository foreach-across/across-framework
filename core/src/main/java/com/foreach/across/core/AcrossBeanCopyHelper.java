package com.foreach.across.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;

public class AcrossBeanCopyHelper
{
	private Map<String, Object> singletonsCopied = new HashMap<String, Object>();
	private Map<String, BeanDefinition> definitionsCopied = new HashMap<String, BeanDefinition>();
	private List<ApplicationListener> applicationListeners = new LinkedList<ApplicationListener>();

	public Map<String, Object> getSingletonsCopied() {
		return singletonsCopied;
	}

	public Map<String, BeanDefinition> getDefinitionsCopied() {
		return definitionsCopied;
	}

	public List<ApplicationListener> getApplicationListeners() {
		return applicationListeners;
	}

	public void copy( ConfigurableApplicationContext child, ConfigurableApplicationContext parent ) {
		List<String> singletons = Arrays.asList( child.getBeanFactory().getSingletonNames() );

		BeanDefinitionRegistry registry = null;

		if ( parent instanceof BeanDefinitionRegistry ) {
			registry = (BeanDefinitionRegistry) parent;
		}

		for ( String defName : child.getBeanDefinitionNames() ) {
			BeanDefinition def = child.getBeanFactory().getBeanDefinition( defName );

			if ( !StringUtils.startsWithIgnoreCase( defName, "org.springframework" ) ) {

				if ( singletons.contains( defName ) ) {
					Object bean = child.getBean( defName );

					parent.getBeanFactory().registerSingleton( defName, bean );

					singletonsCopied.put( defName, bean );

					if ( !( def instanceof GenericBeanDefinition ) && registry != null ) {
						registry.registerBeanDefinition( defName, def );
						definitionsCopied.put( defName, def );
					}
				}
				else if ( registry != null ) {
					// Copy definition
					registry.registerBeanDefinition( defName, def );

					definitionsCopied.put( defName, def );
				}
			}
		}
	}

	public void copyApplicationListeners( ConfigurableApplicationContext child, ConfigurableApplicationContext parent ) {
		ConfigurableListableBeanFactory beanFactory = child.getBeanFactory();
		List<String> singletons = Arrays.asList( child.getBeanFactory().getSingletonNames() );

		for ( String name : singletons ) {
			Object singleton = beanFactory.getSingleton( name );

			if ( singleton instanceof ApplicationListener ) {
				applicationListeners.add( (ApplicationListener) singleton );
				parent.addApplicationListener( (ApplicationListener) singleton );
			}
		}
	}
}
