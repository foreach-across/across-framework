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
package com.foreach.across.core.context.web;

import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
public class TestAcrossWebApplicationContext
{
	private AcrossConfigurableApplicationContext ctx;

	@Before
	public void before() {
		ctx = new AcrossWebApplicationContext();
	}

	@After
	public void after() {
		ctx.stop();
		ctx.close();
		ctx = null;
	}

	@Test
	public void providedBeansBeforeRefresh() {
		ctx.register( BeanReference.class );
		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );
		ctx.refresh();
		ctx.start();

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );

		String[] afterFirstRefresh = ctx.getBeanFactory().getBeanDefinitionNames();

		ctx.refresh();
		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );

		assertArrayEquals( afterFirstRefresh, ctx.getBeanFactory().getBeanDefinitionNames() );
	}

	@Test
	public void providedBeansAfterRefresh() {
		ctx.refresh();
		ctx.start();

		ctx.register( BeanReference.class );
		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );

		ctx.refresh();

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
	}

	@Test
	public void conditionalBeforeRefresh() {
		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );
		ctx.register( ConditionalBeanReference.class );
		ctx.refresh();
		ctx.start();

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
	}

	@Test
	public void conditionalAfterRefresh() {
		ctx.refresh();
		ctx.start();

		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );
		ctx.register( ConditionalBeanReference.class );

		ctx.refresh();

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
	}

	static class SomeBean
	{
	}

	static class BeanReference
	{
		public final SomeBean someBean;

		@Autowired
		public BeanReference( SomeBean someBean ) {
			this.someBean = someBean;
		}
	}

	@AcrossCondition("@someBean != null")
	static class ConditionalBeanReference extends BeanReference
	{
		@Autowired
		public ConditionalBeanReference( SomeBean someBean ) {
			super( someBean );
		}
	}
}
