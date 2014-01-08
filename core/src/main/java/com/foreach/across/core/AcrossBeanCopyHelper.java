package com.foreach.across.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcrossBeanCopyHelper
{
	private Map<String, Object> singletonsCopied = new HashMap<String, Object>();
	private Map<String, BeanDefinition> definitionsCopied = new HashMap<String, BeanDefinition>();

	public Map<String, Object> getSingletonsCopied() {
		return singletonsCopied;
	}

	public Map<String, BeanDefinition> getDefinitionsCopied() {
		return definitionsCopied;
	}

	public void copy( GenericApplicationContext child, GenericApplicationContext parent ) {
		List<String> singletons = Arrays.asList( child.getBeanFactory().getSingletonNames() );

		for ( String defName : child.getBeanDefinitionNames() ) {
			BeanDefinition def = child.getBeanDefinition( defName );

			if ( !StringUtils.startsWithIgnoreCase( defName, "org.springframework" ) ) {

				if ( singletons.contains( defName ) ) {
					Object bean = child.getBean( defName );

					parent.getBeanFactory().registerSingleton( defName, bean );

					singletonsCopied.put( defName, bean );

					if ( !( def instanceof GenericBeanDefinition ) ) {
						parent.registerBeanDefinition( defName, def );
						definitionsCopied.put( defName, def );
					}

					if ( bean instanceof ApplicationListener ) {
						parent.addApplicationListener( (ApplicationListener) bean );
					}
				}
				else {
					// Copy definition
					parent.registerBeanDefinition( defName, def );

					definitionsCopied.put( defName, def );
				}
			}
		}
	}
}
