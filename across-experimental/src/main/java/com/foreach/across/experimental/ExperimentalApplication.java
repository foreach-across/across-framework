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
package com.foreach.across.experimental;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.FilterType;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.util.Collections;

import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME;

/**
 * @author Arne Vandamme
 * @since 4.0.0
 */
@Slf4j
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) }
)
public class ExperimentalApplication
{
	public static void main( String[] args ) {
		SpringApplication application = new AcrossApplication();
		application.addPrimarySources( Collections.singleton( ExperimentalApplication.class ) );
		application.run();

	}

	static class AcrossApplication extends SpringApplication
	{
		@Override
		@SneakyThrows
		protected ConfigurableApplicationContext createApplicationContext() {
			ConfigurableApplicationContext context = super.createApplicationContext();
			Class<? extends ConfigurableApplicationContext> actualClass = context.getClass();

			// get constructor which takes bean factory
			// initiate with the custom bean factory

			Constructor<? extends ConfigurableApplicationContext> constructor = actualClass.getConstructor( DefaultListableBeanFactory.class );
			Assert.notNull( constructor, "no constructor for DefaultListableBeanFactory" );

			ConfigurableApplicationContext configurableApplicationContext = constructor.newInstance( new AcrossListableBeanFactory() );
			configurableApplicationContext.addBeanFactoryPostProcessor( new Registrar() );
			return configurableApplicationContext;
		}
	}

	static class Registrar implements BeanDefinitionRegistryPostProcessor
	{
		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {

		}

		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
			try {
				RootBeanDefinition beanDefinition = (RootBeanDefinition) registry.getBeanDefinition( CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME );
				beanDefinition.setBeanClass( Config.class );
			}
			catch ( NoSuchBeanDefinitionException ex ) {
				LOG.error( "Unable to modify the ConfigurationClassPostProcessor for Across", ex );
			}
		}
	}

	static class Config extends ConfigurationClassPostProcessor
	{
		@Override
		public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) {
			super.postProcessBeanDefinitionRegistry( registry );
		}

		@Override
		public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
			super.postProcessBeanFactory( beanFactory );
		}
	}
}
