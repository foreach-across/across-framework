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

package com.foreach.across.test.filters;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.core.context.ApplicationContextScanner;
import com.foreach.across.core.filters.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestBeanFilters.Config.class)
@DirtiesContext
public class TestBeanFilters
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private BeanWithServiceAnnotation beanWithServiceAnnotation;

	@Autowired
	private BeanWithExposedAnnotation beanWithExposedAnnotation;

	@Autowired
	private BeanWithRefreshableAnnotation beanWithRefreshableAnnotation;

	@Autowired
	private BeanWithExposedAnnotation otherBeanWithRefreshableAnnotation;

	@Autowired
	private BeanWithRefreshableAnnotation otherBeanWithExposedAnnotation;

	@Test
	public void filterByClass() {
		BeanFilter filter = new ClassBeanFilter( BeanWithServiceAnnotation.class );

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 1, beans.size() );
		assertSame( beanWithServiceAnnotation, beans.get( "beanWithServiceAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 2, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithServiceAnnotation" ) );
		assertTrue( definitions.containsKey( "prototypeExposedBean" ) );
	}

	@Test
	public void filterByInterface() {
		BeanFilter filter = new ClassBeanFilter( Marker.class );

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 3, beans.size() );
		assertSame( beanWithExposedAnnotation, beans.get( "beanWithExposedAnnotation" ) );
		assertSame( otherBeanWithRefreshableAnnotation, beans.get( "otherBeanWithRefreshableAnnotation" ) );
		assertSame( beanWithServiceAnnotation, beans.get( "beanWithServiceAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 4, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithServiceAnnotation" ) );
		assertTrue( definitions.containsKey( "prototypeExposedBean" ) );
		assertTrue( definitions.containsKey( "beanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithRefreshableAnnotation" ) );
	}

	@Test
	public void filterByPackage() {
		BeanFilter filter = new PackageBeanFilter( "com.foreach.across.test.filters" );

		// The @Configuration class also matches as a singleton
		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 6, beans.size() );

		// The prototype bean definition also is present in the BeanDefinition list
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 7, definitions.size() );
	}

	@Test
	public void filterOnServiceAnnotation() {
		BeanFilter filter = new AnnotationBeanFilter( Service.class );

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 1, beans.size() );
		assertSame( beanWithServiceAnnotation, beans.get( "beanWithServiceAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 2, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithServiceAnnotation" ) );
		assertTrue( definitions.containsKey( "prototypeExposedBean" ) );
	}

	@Test
	public void filterOnExposedAnnotation() {
		BeanFilter filter = new AnnotationBeanFilter( Exposed.class );

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 3, beans.size() );
		assertSame( beanWithExposedAnnotation, beans.get( "beanWithExposedAnnotation" ) );
		assertSame( otherBeanWithExposedAnnotation, beans.get( "otherBeanWithExposedAnnotation" ) );
		assertSame( otherBeanWithRefreshableAnnotation, beans.get( "otherBeanWithRefreshableAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 4, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithRefreshableAnnotation" ) );
		assertTrue( definitions.containsKey( "prototypeExposedBean" ) );
	}

	@Test
	public void filterOnRefreshableAnnotation() {
		BeanFilter filter = new AnnotationBeanFilter( Refreshable.class );

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 3, beans.size() );
		assertSame( beanWithRefreshableAnnotation, beans.get( "beanWithRefreshableAnnotation" ) );
		assertSame( otherBeanWithExposedAnnotation, beans.get( "otherBeanWithExposedAnnotation" ) );
		assertSame( otherBeanWithRefreshableAnnotation, beans.get( "otherBeanWithRefreshableAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 3, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithRefreshableAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithRefreshableAnnotation" ) );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void filterOnServiceAndExposedAnnotation() {
		BeanFilter filter = new BeanFilterComposite(
				new AnnotationBeanFilter( Service.class ),
				new AnnotationBeanFilter( true, true, Exposed.class )
		);

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( applicationContext, filter );
		assertEquals( 4, beans.size() );
		assertSame( beanWithExposedAnnotation, beans.get( "beanWithExposedAnnotation" ) );
		assertSame( otherBeanWithExposedAnnotation, beans.get( "otherBeanWithExposedAnnotation" ) );
		assertSame( otherBeanWithRefreshableAnnotation, beans.get( "otherBeanWithRefreshableAnnotation" ) );
		assertSame( beanWithServiceAnnotation, beans.get( "beanWithServiceAnnotation" ) );

		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( applicationContext, filter );
		assertEquals( 5, definitions.size() );
		assertTrue( definitions.containsKey( "beanWithServiceAnnotation" ) );
		assertTrue( definitions.containsKey( "beanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithExposedAnnotation" ) );
		assertTrue( definitions.containsKey( "otherBeanWithRefreshableAnnotation" ) );
		assertTrue( definitions.containsKey( "prototypeExposedBean" ) );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public BeanWithServiceAnnotation beanWithServiceAnnotation() {
			return new BeanWithServiceAnnotation();
		}

		@Bean
		public BeanWithRefreshableAnnotation beanWithRefreshableAnnotation() {
			return new BeanWithRefreshableAnnotation();
		}

		@Bean
		public BeanWithExposedAnnotation beanWithExposedAnnotation() {
			return new BeanWithExposedAnnotation();
		}

		@Bean
		@Exposed
		public BeanWithRefreshableAnnotation otherBeanWithExposedAnnotation() {
			// Also has the refreshable annotation
			return new BeanWithRefreshableAnnotation();
		}

		@Bean
		@Refreshable
		public BeanWithExposedAnnotation otherBeanWithRefreshableAnnotation() {
			// Also has the exposed annotation
			return new BeanWithExposedAnnotation();
		}

		@Bean
		@Scope("prototype")
		@Exposed
		public BeanWithServiceAnnotation prototypeExposedBean() {
			return new BeanWithServiceAnnotation();
		}
	}

	public interface Marker
	{

	}

	@Service
	public static class BeanWithServiceAnnotation implements Marker
	{
	}

	@Refreshable
	public static class BeanWithRefreshableAnnotation
	{
	}

	@Exposed
	public static class BeanWithExposedAnnotation implements Marker
	{
	}

}
