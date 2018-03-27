/*
 * Copyright 2014 the original author or authors
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
package com.foreach.across.core.events;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;

import java.util.List;

import static org.springframework.context.annotation.AnnotationConfigUtils.EVENT_LISTENER_PROCESSOR_BEAN_NAME;

/**
 * Extension of {@link EventListenerMethodProcessor} to be injected in modules.
 * It only registers event listener methods from non-exposed beans. The ones from exposed beans will have been registered previously in another module.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public final class NonExposedEventListenerMethodProcessor extends EventListenerMethodProcessor
{
	private ConfigurableApplicationContext applicationContext;

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		super.setApplicationContext( applicationContext );
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	protected void processBean( List<EventListenerFactory> factories, String beanName, Class<?> targetType ) {
		if ( !( (AcrossListableBeanFactory) applicationContext.getBeanFactory() ).isExposedBean( beanName ) ) {
			super.processBean( factories, beanName, targetType );
		}
	}

	/**
	 * Registers the Across compatible {@link EventListenerMethodProcessor}, replaces the original one from Spring.
	 */
	public static class Registrar implements BeanDefinitionRegistryPostProcessor
	{
		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
			if ( registry.containsBeanDefinition( EVENT_LISTENER_PROCESSOR_BEAN_NAME ) ) {
				registry.removeBeanDefinition( EVENT_LISTENER_PROCESSOR_BEAN_NAME );
			}

			RootBeanDefinition def = new RootBeanDefinition( NonExposedEventListenerMethodProcessor.class );
			def.setSource( this );
			def.setRole( BeanDefinition.ROLE_INFRASTRUCTURE );
			registry.registerBeanDefinition( EVENT_LISTENER_PROCESSOR_BEAN_NAME, def );
		}

		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
		}
	}
}
