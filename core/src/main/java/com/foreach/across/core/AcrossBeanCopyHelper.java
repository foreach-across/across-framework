package com.foreach.across.core;

import com.foreach.across.core.events.AcrossContextEventListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;

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

		if ( parent.getBeanFactory() instanceof BeanDefinitionRegistry ) {
			registry = (BeanDefinitionRegistry) parent.getBeanFactory();
		}

		for ( String defName : child.getBeanDefinitionNames() ) {
			BeanDefinition def = child.getBeanFactory().getBeanDefinition( defName );

			if ( !StringUtils.startsWithIgnoreCase( defName, "org.springframework" ) ) {

				if ( singletons.contains( defName ) ) {
					Object bean = child.getBean( defName );

					parent.getBeanFactory().registerSingleton( defName, bean );

					singletonsCopied.put( defName, bean );

//					if ( !( def instanceof GenericBeanDefinition ) && registry != null ) {
//						registry.registerBeanDefinition( defName, def );
//						definitionsCopied.put( defName, def );
//					}
				}
				else if ( registry != null ) {
					// Copy definition
					registry.registerBeanDefinition( defName, def );

					definitionsCopied.put( defName, def );
				}
			}
		}
	}

	public void copyApplicationListeners(
			ConfigurableApplicationContext child, ConfigurableApplicationContext parent ) {
		ApplicationEventMulticaster childMulticaster = child.getBean( ApplicationEventMulticaster.class );
		Collection<AcrossContextEventListener> contextEventListeners =
				child.getBeansOfType( AcrossContextEventListener.class ).values();

		// Only an AcrossContextEventListener is moved up to the parent context and removed from the child
		for ( AcrossContextEventListener bean : contextEventListeners ) {
			parent.addApplicationListener( bean );
			childMulticaster.removeApplicationListener( bean );
		}
	}
}
