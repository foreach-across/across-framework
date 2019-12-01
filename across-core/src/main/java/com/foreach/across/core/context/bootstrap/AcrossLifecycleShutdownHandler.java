/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Responsible for shutting down an {@link com.foreach.across.core.AcrossContext}.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RequiredArgsConstructor
@Slf4j
public final class AcrossLifecycleShutdownHandler
{
	private final AcrossContext acrossContext;

	public void shutdown() {
		AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo( acrossContext );

		if ( contextInfo == null ) {
			return;
		}

		AcrossConfigurableApplicationContext rootApplicationContext = (AcrossConfigurableApplicationContext) contextInfo.getApplicationContext();
		removeIntroducedParentContext( rootApplicationContext );

		removeExposedBeanDefinitions( rootApplicationContext );
		contextInfo.getModules()
		           .stream()
		           .filter( AcrossModuleInfo::isBootstrapped )
		           .map( AcrossModuleInfo::getApplicationContext )
		           .forEach( this::removeExposedBeanDefinitions );

		// Shutdown all modules in reverse order - note that it is quite possible that beans might have been destroyed
		// already by Spring in the meantime
		List<AcrossModuleInfo> reverseList = new ArrayList<>( contextInfo.getModules() );
		Collections.reverse( reverseList );

		for ( AcrossModuleInfo moduleInfo : reverseList ) {
			if ( moduleInfo.isBootstrapped() ) {
				AcrossModule module = moduleInfo.getModule();
				AcrossConfigurableApplicationContext applicationContext = AcrossContextUtils.getApplicationContext( module );

				if ( applicationContext != null ) {
					LOG.debug( "Destroying ApplicationContext for module {}", module.getName() );

					applicationContext.close();
					AcrossContextUtils.setAcrossApplicationContextHolder( module, null );
				}
			}
		}

		// Destroy the root ApplicationContext

		rootApplicationContext.close();
		AcrossContextUtils.setAcrossApplicationContextHolder( acrossContext, null );
		LOG.debug( "Destroyed root ApplicationContext: {}", acrossContext.getId() );
	}

	/**
	 * Attempt to cleanup automatically introduced application contexts.
	 * This does some internal "hacking" as default spring behaviour does not allow easy removal of parent contexts.
	 */
	private void removeIntroducedParentContext( ApplicationContext applicationContext ) {
		if ( applicationContext != null ) {
			ApplicationContext parent = applicationContext.getParent();
			if ( parent != null && AcrossBootstrapper.EXPOSE_SUPPORTING_APPLICATION_CONTEXT.equals( parent.getId() ) ) {
				LOG.debug( "Attempting to remove automatically introduced ApplicationContext supporting exposed bean definitions: {}", parent.getId() );
				ConfigurableApplicationContext originalApplicationContext = (ConfigurableApplicationContext) applicationContext;
				ConfigurableListableBeanFactory originalBeanFactory = originalApplicationContext.getBeanFactory();

				ApplicationContext newParentApplicationContext = parent.getParent();
				BeanFactory newParentBeanFactory = null;
				if ( newParentApplicationContext != null ) {
					newParentBeanFactory = ( (HierarchicalBeanFactory) newParentApplicationContext.getAutowireCapableBeanFactory() ).getParentBeanFactory();
				}

				updateParentBeanFactory( originalBeanFactory, newParentBeanFactory );
				updateParentApplicationContext( originalApplicationContext, newParentApplicationContext );

				updateParentBeanFactory( parent.getAutowireCapableBeanFactory(), null );
				updateParentApplicationContext( parent, null );

				removeExposedBeanDefinitions( parent );
				( (AcrossConfigurableApplicationContext) parent ).close();
			}
			else if ( parent instanceof ConfigurableApplicationContext ) {
				removeIntroducedParentContext( parent );
			}
		}
	}

	private void removeExposedBeanDefinitions( ApplicationContext applicationContext ) {
		// todo: refactor, remove from the central registry
		AcrossListableBeanFactory beanFactory = (AcrossListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		Stream.of( beanFactory.getBeanDefinitionNames() )
		      .filter( beanFactory::isExposedBean )
		      .forEach( beanFactory::removeBeanDefinition );

	}

	private void updateParentBeanFactory( BeanFactory beanFactory, BeanFactory parentBeanFactory ) {
		Field field = ReflectionUtils.findField( beanFactory.getClass(), "parentBeanFactory" );
		if ( field != null ) {
			field.setAccessible( true );
			try {
				field.set( beanFactory, parentBeanFactory );
			}
			catch ( Exception e ) {
				LOG.warn( "Unable to remove artificially introduced BeanFactory", e );
			}
		}
		else {
			LOG.warn( "Unable to remove artificially introduced BeanFactory" );
		}
	}

	private void updateParentApplicationContext( ApplicationContext applicationContext, ApplicationContext parentApplicationContext ) {
		Field field = ReflectionUtils.findField( applicationContext.getClass(), "parent" );
		if ( field != null ) {
			field.setAccessible( true );
			try {
				field.set( applicationContext, parentApplicationContext );
			}
			catch ( Exception e ) {
				LOG.warn( "Unable to remove artificially introduced ApplicationContext", e );
			}
		}
		else {
			LOG.warn( "Unable to remove artificially introduced ApplicationContext" );
		}
	}
}
