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
package com.foreach.across.boot;

import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Registers extensions determined by auto-configuration classes that have been requested.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 * @see AcrossApplicationAutoConfiguration
 */
@Configuration
public class ExtendModuleAutoConfiguration implements AcrossBootstrapConfigurer, BeanClassLoaderAware, BeanFactoryAware
{
	private final static Logger LOG = LoggerFactory.getLogger( ExtendModuleAutoConfiguration.class );

	private ConfigurableListableBeanFactory beanFactory;

	private ClassLoader beanClassLoader;

	@Override
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
		Assert.isInstanceOf( ConfigurableListableBeanFactory.class, beanFactory );
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public void setBeanClassLoader( ClassLoader classLoader ) {
		this.beanClassLoader = classLoader;
	}

	@Override
	public void configureContext( AcrossBootstrapConfig contextConfiguration ) {
		AcrossApplicationAutoConfiguration
				.retrieve( beanFactory, beanClassLoader )
				.getModuleExtensions()
				.forEach( ( moduleName, classNames ) -> {
					Class[] classes = classNames
							.stream()
							.filter( c -> ClassUtils.isPresent( c, beanClassLoader ) )
							.map( c -> {
								try {
									return ClassUtils.forName( c, beanClassLoader );
								}
								catch ( ClassNotFoundException cnfe ) {
									LOG.error( "Unable to instantiate class {}", c );
									return null;
								}
							} )
							.filter( Objects::nonNull )
							.toArray( Class[]::new );

					if ( LOG.isTraceEnabled() ) {
						Stream.of( classes )
						      .forEach( annotatedClass -> LOG.trace( "Extending module {} with class {}", moduleName, annotatedClass.getName() ) );
					}

					contextConfiguration.extendModule( moduleName, classes );
				} );
	}
}
