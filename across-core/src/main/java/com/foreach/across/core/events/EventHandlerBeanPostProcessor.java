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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Responsible for scanning every bean for {@link com.foreach.across.core.annotations.Event} methods.
 * Registers itself as a {@link BeanPostProcessor} in the current and every parent {@link BeanFactory}.
 * Also scans any already existing singletons from the current and parent factories for event handler methods,
 * any pre-existing non-singletons are ignored.  Non-singletons that are created after this processor has
 * been added will get scanned however.
 * <p/>
 * This processor does not modify the beans in any way.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.context.AcrossApplicationContext#registerEventHandlerBeanPostProcessor(ConfigurableListableBeanFactory)
 * @since 2.0.0
 */
public class EventHandlerBeanPostProcessor implements BeanPostProcessor
{
	private final BeanFactory beanFactory;

	public EventHandlerBeanPostProcessor(
			ConfigurableListableBeanFactory beanFactory ) {
		this.beanFactory = beanFactory;

		addPostProcessorToBeanFactoryHierarchy( beanFactory );
		registerExistingSingletons( beanFactory );
	}

	private void registerExistingSingletons( ConfigurableListableBeanFactory beanFactory ) {
		for ( String singletonName : beanFactory.getSingletonNames() ) {
			register( beanFactory.getSingleton( singletonName ) );
		}

		BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
		if ( parentBeanFactory instanceof ConfigurableListableBeanFactory ) {
			registerExistingSingletons( (ConfigurableListableBeanFactory) parentBeanFactory );
		}
	}

	private void addPostProcessorToBeanFactoryHierarchy( ConfigurableBeanFactory beanFactory ) {
		beanFactory.addBeanPostProcessor( this );

		BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
		if ( parentBeanFactory instanceof ConfigurableBeanFactory ) {
			addPostProcessorToBeanFactoryHierarchy( (ConfigurableBeanFactory) parentBeanFactory );
		}
	}

	@Override
	public Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
		register( bean );
		return bean;
	}

	private void register( Object bean ) {
		beanFactory.getBean( AcrossEventPublisher.class ).subscribe( bean );
	}
}
