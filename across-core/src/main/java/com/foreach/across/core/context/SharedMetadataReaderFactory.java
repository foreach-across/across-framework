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
package com.foreach.across.core.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.type.classreading.ConcurrentReferenceCachingMetadataReaderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * Registers a shared metadata reader for all Across related contexts.
 * In a Spring boot configuration, an already available metadata reader factory will be reused.
 *
 * @author Arne Vandamme
 * @see org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer
 * @since 3.0.0
 */
public class SharedMetadataReaderFactory
{
	public static final String BEAN_NAME = "com.foreach.across.core.context.sharedMetadataReaderFactory";

	private static final String SPRING_BOOT_SHARED_METADATA_FACTORY = "org.springframework.boot.autoconfigure."
			+ "internalCachingMetadataReaderFactory";

	/**
	 * {@link FactoryBean} to create the shared {@link MetadataReaderFactory}.
	 */
	static class SharedMetadataReaderFactoryBean
			implements FactoryBean<ConcurrentReferenceCachingMetadataReaderFactory>,
			BeanClassLoaderAware, BeanFactoryAware
	{
		private ConcurrentReferenceCachingMetadataReaderFactory metadataReaderFactory;
		private ConcurrentReferenceCachingMetadataReaderFactory parentReaderFactory;

		@Override
		public void setBeanClassLoader( ClassLoader classLoader ) {
			this.metadataReaderFactory = new ConcurrentReferenceCachingMetadataReaderFactory( classLoader );
		}

		@Override
		public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
			try {
				parentReaderFactory = beanFactory.getBean( SPRING_BOOT_SHARED_METADATA_FACTORY, ConcurrentReferenceCachingMetadataReaderFactory.class );
			}
			catch ( NoSuchBeanDefinitionException ignore ) {
			}
		}

		@Override
		public ConcurrentReferenceCachingMetadataReaderFactory getObject()
				throws Exception {
			return this.parentReaderFactory != null ? this.parentReaderFactory : this.metadataReaderFactory;
		}

		@Override
		public Class<?> getObjectType() {
			return CachingMetadataReaderFactory.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}

	public static void registerAnnotationProcessors( BeanDefinitionRegistry beanFactory ) {
		register( beanFactory );
		configureConfigurationClassPostProcessor( beanFactory );
	}

	private static void register( BeanDefinitionRegistry registry ) {
		if ( registry instanceof ListableBeanFactory && ( (ListableBeanFactory) registry ).containsBean( BEAN_NAME ) ) {
			return;
		}

		RootBeanDefinition definition = new RootBeanDefinition( SharedMetadataReaderFactoryBean.class );
		registry.registerBeanDefinition( BEAN_NAME, definition );
	}

	private static void configureConfigurationClassPostProcessor( BeanDefinitionRegistry registry ) {
		try {

			BeanDefinition definition = registry.getBeanDefinition( AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME );
			definition.getPropertyValues().add( "metadataReaderFactory", new RuntimeBeanReference( BEAN_NAME ) );
		}
		catch ( NoSuchBeanDefinitionException ignore ) {
		}
	}

	public static void clearCachedMetadata( ApplicationContext applicationContext ) {
		ConcurrentReferenceCachingMetadataReaderFactory metadataReaderFactory
				= applicationContext.getBean( BEAN_NAME, ConcurrentReferenceCachingMetadataReaderFactory.class );
		metadataReaderFactory.clearCache();
	}
}
