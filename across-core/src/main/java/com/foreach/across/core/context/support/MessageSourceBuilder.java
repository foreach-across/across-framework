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
package com.foreach.across.core.context.support;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Internal;
import com.foreach.across.core.context.AcrossApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.core.type.MethodMetadata;

import java.util.Optional;

/**
 * Takes care of building the MessageSourceHierarchy associated with an AcrossModule
 * or AcrossContext.  Ensures that the message source is registered under the right
 * bean name in the registry and bean factory provided.
 *
 * @author Arne Vandamme
 */
public class MessageSourceBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( MessageSourceBuilder.class );

	private static final String BEAN_NAME = AcrossApplicationContext.MESSAGE_SOURCE_BEAN_NAME;

	private ConfigurableListableBeanFactory beanFactory;
	private BeanDefinitionRegistry registry;

	public MessageSourceBuilder( ConfigurableListableBeanFactory beanFactory ) {
		this.beanFactory = beanFactory;
		this.registry = (BeanDefinitionRegistry) beanFactory;
	}

	public void build( MessageSource parent ) {
		if ( shouldIncludeMessageSource() ) {
			// Push the entire *internal* hierarchy in there
			HierarchicalMessageSource source = findHighestAvailableMessageSource( beanFactory.getBean( BEAN_NAME,
			                                                                                           MessageSource.class ) );

			if ( source != null && parent != null && parent instanceof AcrossContextOrderedMessageSource ) {
				AcrossContextOrderedMessageSource parentSource = (AcrossContextOrderedMessageSource) parent;
				parentSource.push( source );

				// Remove the original bean definition and singleton from the registry
				registry.removeBeanDefinition( BEAN_NAME );
			}
			else {
				LOG.trace(
						"Not pushing MessageSource to context level as it is either not a HierarchicalMessageSource " +
								"or the parent is not an AcrossContextMessageSource" );
			}

		}
	}

	private boolean shouldIncludeMessageSource() {
		if ( beanFactory.containsBeanDefinition( BEAN_NAME ) ) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition( BEAN_NAME );

			if ( beanDefinition.getSource() instanceof MethodMetadata ) {
				MethodMetadata metadata = (MethodMetadata) beanDefinition.getSource();

				// If method annotated with Internal, should not be included
				if ( metadata.isAnnotated( Internal.class.getName() ) ) {
					return false;
				}
			}

			return true;
		}

		return createDefaultMessageSource();
	}

	private boolean createDefaultMessageSource() {
		Optional<String> moduleName = moduleNameForBeanFactory();

		if ( moduleName.isPresent() ) {
			BeanDefinition rootBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition( AcrossModuleMessageSource.class ).getBeanDefinition();
			registry.registerBeanDefinition( BEAN_NAME, rootBeanDefinition );

			return true;
		}

		return false;
	}

	private Optional<String> moduleNameForBeanFactory() {
		return beanFactory.getBeansOfType( AcrossModule.class )
		                  .keySet().stream()
		                  .filter( beanName -> beanFactory.getBeanDefinition( beanName ).isPrimary() )
		                  .findFirst();
	}

	public static HierarchicalMessageSource findHighestAvailableMessageSource( MessageSource current ) {
		if ( current instanceof HierarchicalMessageSource ) {
			HierarchicalMessageSource source = (HierarchicalMessageSource) current;

			if ( source.getParentMessageSource() != null ) {
				return findHighestAvailableMessageSource( source.getParentMessageSource() );
			}

			return source;
		}

		return null;
	}
}
