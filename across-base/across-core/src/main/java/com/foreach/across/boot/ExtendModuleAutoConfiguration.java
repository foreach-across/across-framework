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

import com.foreach.across.core.context.beans.ProvidedBeansMap;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ProvidedBeansConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Stream;

/**
 * Registers extensions determined by auto-configuration classes that have been requested.
 *
 * @author Arne Vandamme
 * @see AcrossApplicationAutoConfiguration
 * @since 3.0.0
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
	public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
		String moduleName = moduleConfiguration.getModuleName();
		AcrossApplicationAutoConfiguration applicationAutoConfiguration = AcrossApplicationAutoConfiguration.retrieve( beanFactory, beanClassLoader );
		Map<String, List<String>> moduleExtensions = applicationAutoConfiguration.getModuleExtensions();

		Set<String> classNames = new TreeSet<>( Comparator.comparingInt( applicationAutoConfiguration::getAutoConfigurationOrder ) );
		Stream.of( moduleConfiguration.getAllModuleNames() )
		      .forEach( name -> classNames.addAll( moduleExtensions.getOrDefault( name, Collections.emptyList() ) ) );

		if ( !classNames.isEmpty() ) {
			classNames.forEach( annotatedClass -> LOG.trace( "Extending module {} with class {}", moduleName, annotatedClass ) );

			AutoConfigurationModuleExtension moduleExtension = new AutoConfigurationModuleExtension( classNames );
			ProvidedBeansMap beans = new ProvidedBeansMap();
			beans.put( AutoConfigurationModuleExtension.BEAN, moduleExtension );

			moduleConfiguration.addApplicationContextConfigurer( new ProvidedBeansConfigurer( beans ) );
			moduleConfiguration.addApplicationContextConfigurer( AutoConfigurationModuleExtension.Registrar.class );
		}
	}
}
