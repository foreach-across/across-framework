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
package com.foreach.across.test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import com.foreach.across.test.modules.exposing.ExposingModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.ConversionService;

import static org.junit.Assert.*;

/**
 * Test @Exposed functionality within the AcrossContext.
 *
 * @author Arne Vandamme
 */
public class TestAcrossContextExposedFilters
{
	private GenericApplicationContext parent;
	private AcrossContext context;

	@Before
	public void createContexts() {
		parent = new GenericApplicationContext();
		parent.refresh();

		context = new AcrossContext( parent );
	}

	@After
	public void destroyContexts() {
		context.shutdown();
		parent.close();
	}

	@Test
	public void defaultContextExposedBeans() {
		context.bootstrap();

		assertTrue( parent.containsBean( "cacheManager" ) );
		assertTrue( parent.containsBean( "conversionService" ) );

		assertNotNull( parent.getBean( AcrossContextInfo.class ) );
		assertNotNull( parent.getBean( "cacheManager", CacheManager.class ) );
		assertNotNull( parent.getBean( "conversionService", ConversionService.class ) );
	}

	@Test
	public void transformingExposedBeans() {
		context.setExposeTransformer( new BeanPrefixingTransformer( "across" ) );
		context.bootstrap();

		assertFalse( parent.containsBean( "cacheManager" ) );
		assertFalse( parent.containsBean( "conversionService" ) );

		assertNotNull( parent.getBean( "acrossCacheManager", CacheManager.class ) );
		assertNotNull( parent.getBean( "acrossConversionService", ConversionService.class ) );

		ApplicationContext across = AcrossContextUtils.getApplicationContext( context );
		assertTrue( across.containsBean( "cacheManager" ) );
		assertTrue( across.containsBean( "conversionService" ) );

		assertNotNull( across.getBean( AcrossContextInfo.class ) );
		assertNotNull( across.getBean( "cacheManager", CacheManager.class ) );
		assertNotNull( across.getBean( "conversionService", ConversionService.class ) );
	}

	@Test
	public void disableExposing() {
		int initialBeanCount = BeanFactoryUtils.countBeansIncludingAncestors( parent );

		context.setExposeTransformer( ExposedBeanDefinitionTransformer.REMOVE_ALL );
		context.bootstrap();

		assertFalse( parent.containsBean( "cacheManager" ) );
		assertFalse( parent.containsBean( "conversionService" ) );
		assertEquals( initialBeanCount, BeanFactoryUtils.countBeansIncludingAncestors( parent ) );

		ApplicationContext across = AcrossContextUtils.getApplicationContext( context );
		assertTrue( across.containsBean( "cacheManager" ) );
		assertTrue( across.containsBean( "conversionService" ) );

		assertNotNull( across.getBean( AcrossContextInfo.class ) );
		assertNotNull( across.getBean( "cacheManager", CacheManager.class ) );
		assertNotNull( across.getBean( "conversionService", ConversionService.class ) );
	}

	@Test
	public void transformerOnContextAndModule() {
		ExposingModule withTransformer = new ExposingModule( "withTransformer" );
		withTransformer.setExposeTransformer( new BeanPrefixingTransformer( "moduleTransformed" ) );

		context.addModule( withTransformer );
		context.setExposeTransformer( new BeanPrefixingTransformer( "contextTransformed" ) );
		context.bootstrap();

		assertTrue( parent.containsBean( "contextTransformedModuleTransformedMyService" ) );
		assertFalse( parent.containsBean( "moduleTransformedMyService" ) );

		Object instance = parent.getBean( "contextTransformedModuleTransformedMyService" );

		ApplicationContext across = AcrossContextUtils.getApplicationContext( context );
		assertTrue( across.containsLocalBean( "moduleTransformedMyService" ) );
		Object other = across.getBean( "moduleTransformedMyService" );

		assertNotNull( instance );
		assertSame( instance, other );
	}
}
