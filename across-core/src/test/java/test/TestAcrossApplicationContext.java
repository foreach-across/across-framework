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
package test;

import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.configurer.SingletonBeanConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * @author Arne Vandamme
 */
public class TestAcrossApplicationContext
{
	private AcrossApplicationContext ctx;

	@Before
	public void before() {
		ctx = new AcrossApplicationContext();
	}

	@After
	public void after() {
		ctx.stop();
		ctx.close();
		ctx = null;
	}

	@Test
	public void installerModeAcrossApplicationContextInitializesItsOwnEventMulticaster() {
		AcrossApplicationContext parent = new AcrossApplicationContext();
		parent.refresh();
		parent.start();

		ctx.setInstallerMode( true );
		ctx.setParent( parent );
		ctx.refresh();
		ctx.start();

		assertNotSame(
				parent.getBean( AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME ),
				ctx.getBean( AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME )
		);
	}

	@Test
	public void acrossApplicationContextInitializesItsOwnEventMulticasterIfThereIsNoParent() {
		ctx.refresh();
		ctx.start();

		assertNotNull( ctx.getBean( AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME ) );
	}

	@Test
	public void acrossApplicationContextInitializesItsOwnEventMulticasterIfParentIsNotAcross() {
		GenericApplicationContext parent = new GenericApplicationContext();
		parent.refresh();
		parent.start();

		ctx.setParent( parent );
		ctx.refresh();
		ctx.start();

		assertNotSame(
				parent.getBean( AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME ),
				ctx.getBean( AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME )
		);
	}

	@Test
	public void providedBeansBeforeRefresh() {
		ctx.register( BeanReference.class );
		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );
		ctx.refresh();
		ctx.start();

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
//
//		String[] afterFirstRefresh = ctx.getBeanFactory().getBeanDefinitionNames();
//
//		ctx.refresh();
//		assertNotNull( ctx.getBean( "someBean" ) );
//		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
//
//		assertArrayEquals( afterFirstRefresh, ctx.getBeanFactory().getBeanDefinitionNames() );
	}

	@Test
	public void providedBeansAfterRefresh() {
		ctx.refresh();
		ctx.start();

		ctx.register( BeanReference.class );
		ctx.provide( new SingletonBeanConfigurer( "someBean", new SomeBean() ).providedBeans() );

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

		assertNotNull( ctx.getBean( "someBean" ) );
		assertNotNull( ctx.getBean( BeanReference.class ).someBean );
	}

	static class SomeBean
	{
	}

	static class BeanReference
	{
		final SomeBean someBean;

		@Autowired
		public BeanReference( SomeBean someBean ) {
			this.someBean = someBean;
		}
	}

	@ConditionalOnExpression("@someBean != null")
	static class ConditionalBeanReference extends BeanReference
	{
		@Autowired
		public ConditionalBeanReference( SomeBean someBean ) {
			super( someBean );
		}
	}
}
