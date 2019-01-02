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

package test.spring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import test.modules.exposing.LazyExposedBean;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * DevelopmentModeCondition general approach of Spring ApplicationContext hierarchy and the way to expose beans
 * using custom bean definitions.
 *
 * @author Arne Vandamme
 */
public class TestCustomBeanDefinitions
{
	private GenericApplicationContext parent;
	private AnnotationConfigApplicationContext childOne, childTwo;

	private CustomPostProcessor parentProcessor, childOneProcessor, childTwoProcessor;

	@Before
	public void createContext() {
		LazyExposedBean.reset();

		parent = new GenericApplicationContext();
		parent.setDisplayName( "parent" );
		parentProcessor = new CustomPostProcessor();
		parent.getBeanFactory().addBeanPostProcessor( parentProcessor );
		parent.refresh();

		childOne = new AnnotationConfigApplicationContext();
		childOne.setDisplayName( "childOne" );
		childOne.register( Config.class );
		childOneProcessor = new CustomPostProcessor();
		childOne.getBeanFactory().addBeanPostProcessor( childOneProcessor );
		childOne.setParent( parent );
		childOne.refresh();

		Factory factory = new Factory( childOne );
		parent.getBeanFactory().registerSingleton( "factory", factory );

		childTwo = new AnnotationConfigApplicationContext();
		childTwo.setDisplayName( "childTwo" );
		childTwo.register( OtherConfig.class );
		childTwoProcessor = new CustomPostProcessor();
		childTwo.getBeanFactory().addBeanPostProcessor( childTwoProcessor );
		childTwo.setParent( parent );
		childTwo.refresh();

		Factory factoryTwo = new Factory( childTwo );
		parent.getBeanFactory().registerSingleton( "factoryTwo", factoryTwo );
	}

	@After
	public void destroyContext() {
		childOne.stop();
		childTwo.stop();
		parent.stop();
	}

	@Test
	public void beanExistsOnlyInChild() {
		assertEquals( "myBean", childOne.getBean( "myBean" ) );
		assertEquals( "otherBean", childTwo.getBean( "otherBean" ) );
		assertNull( getBean( parent, "myBean" ) );
		assertNull( getBean( childOne, "otherBean" ) );
		assertNull( getBean( childTwo, "myBean" ) );
	}

	@Test
	public void simpleBeanRegisteredInParent() {
		registerBeanInParent( "factory", "myBean", LazyExposedBean.class );

		assertEquals( "myBean", getBean( parent, "myBean" ) );
		assertEquals( "myBean", getBean( childTwo, "myBean" ) );

		assertTrue( parent.containsBean( "myBean" ) );
		assertTrue( childTwo.containsBean( "myBean" ) );
	}

	@Test
	public void postProcessorShouldNotRunInParentContext() {
		registerBeanInParent( "factory", "myBean", LazyExposedBean.class );

		assertEquals( "myBean", getBean( parent, "myBean" ) );

		assertTrue( childOneProcessor.handled( "myBean" ) );
		assertFalse( childTwoProcessor.handled( "myBean" ) );
		assertFalse( parentProcessor.handled( "myBean" ) );
	}

	@Test
	public void prototypeAlwaysGetsCreated() {
		registerBeanInParent( "factory", "prototypeBean", LazyExposedBean.class );

		Object prototypeOneFromOne = childOne.getBean( "prototypeBean" );
		Object prototypeTwoFromOne = childOne.getBean( "prototypeBean" );
		assertNotNull( prototypeOneFromOne );
		assertNotNull( prototypeTwoFromOne );
		assertNotSame( prototypeOneFromOne, prototypeTwoFromOne );

		Object prototypeOneFromTwo = childTwo.getBean( "prototypeBean" );
		Object prototypeTwoFromTwo = childTwo.getBean( "prototypeBean" );
		assertNotNull( prototypeOneFromTwo );
		assertNotNull( prototypeTwoFromTwo );
		assertNotSame( prototypeOneFromTwo, prototypeTwoFromTwo );
		assertNotSame( prototypeOneFromOne, prototypeOneFromTwo );

		Object prototypeOneFromParent = parent.getBean( "prototypeBean" );
		Object prototypeTwoFromParent = parent.getBean( "prototypeBean" );
		assertNotNull( prototypeOneFromParent );
		assertNotNull( prototypeTwoFromParent );
		assertNotSame( prototypeOneFromParent, prototypeTwoFromParent );
		assertNotSame( prototypeOneFromOne, prototypeOneFromParent );
	}

	@Test
	public void lazyBeanFromChildOnlyGetsCreatedWhenRequestedFirstTime() {
		registerBeanInParent( "factory", "lazyBean", LazyExposedBean.class );

		assertEquals( 0, LazyExposedBean.getCreationCount() );

		LazyExposedBean bean = parent.getBean( LazyExposedBean.class );
		assertNotNull( bean );
		assertEquals( 1, LazyExposedBean.getCreationCount() );

		assertSame( bean, childOne.getBean( "lazyBean" ) );
		assertSame( bean, childOne.getBean( LazyExposedBean.class ) );
		assertSame( bean, childTwo.getBean( "lazyBean" ) );
		assertSame( bean, childTwo.getBean( LazyExposedBean.class ) );
		assertEquals( 1, LazyExposedBean.getCreationCount() );
	}

	@Test
	public void beansAreWiredInTheirCreatingContext() {
		registerBeanInParent( "factory", "firstDependencyForSingleton", DependencyForSingleton.class );
		registerBeanInParent( "factoryTwo", "otherDependencyForSingleton", DependencyForSingleton.class );

		SingletonWithDependency singletonOne = childOne.getBean( SingletonWithDependency.class );
		SingletonWithDependency singletonTwo = childTwo.getBean( SingletonWithDependency.class );

		assertEquals( "first", singletonOne.getName() );
		assertEquals( "first", singletonOne.getDependencyName() );
		assertEquals( "other", singletonTwo.getName() );
		assertEquals( "other", singletonTwo.getDependencyName() );

		registerBeanInParent( "factory", "singletonWithDependencyOne", SingletonWithDependency.class );
		registerBeanInParent( "factoryTwo", "singletonWithDependencyTwo", SingletonWithDependency.class );

		SingletonWithDependency singletonOneFromTwo = (SingletonWithDependency) childTwo.getBean(
				"singletonWithDependencyOne" );
		SingletonWithDependency singletonTwoFromOne = (SingletonWithDependency) childOne.getBean(
				"singletonWithDependencyTwo" );

		assertEquals( "first", singletonOneFromTwo.getDependencyName() );
		assertEquals( "other", singletonTwoFromOne.getDependencyName() );
	}

	private void registerBeanInParent( String factory, String beanName, Class type ) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition( type );
		beanDefinition.setTargetType( type );
		beanDefinition.setBeanClass( type );
		beanDefinition.setBeanClassName( type.getName() );
		beanDefinition.setFactoryBeanName( factory );
		beanDefinition.setFactoryMethodName( "getBean" );
		beanDefinition.setScope( "prototype" );
		beanDefinition.setSynthetic( true );

		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue( beanName );

		beanDefinition.setConstructorArgumentValues( constructorArgumentValues );

		parent.registerBeanDefinition( beanName, beanDefinition );
	}

	class CustomPostProcessor implements BeanPostProcessor
	{
		private Set<String> handled = new HashSet<>();

		@Override
		public Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
			handled.add( beanName );
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
			handled.add( beanName );
			return bean;
		}

		public boolean handled( String beanName ) {
			return handled.contains( beanName );
		}
	}

	class Factory
	{
		private AnnotationConfigApplicationContext applicationContext;

		Factory( AnnotationConfigApplicationContext applicationContext ) {
			this.applicationContext = applicationContext;
		}

		public Object getBean( String name ) {
			return applicationContext.getBean( name );
		}
	}

	private Object getBean( ApplicationContext ctx, String beanName ) {
		try {
			return ctx.getBean( beanName );
		}
		catch ( Exception e ) {
			return null;
		}
	}

	protected static class PrototypeBean
	{
	}

	protected static class Config
	{
		@Bean
		public String myBean() {
			return "myBean";
		}

		@Bean
		@Lazy
		public LazyExposedBean lazyBean() {
			return new LazyExposedBean();
		}

		@Bean
		@Scope("prototype")
		public PrototypeBean prototypeBean() {
			return new PrototypeBean();
		}

		@Bean
		@Scope("prototype")
		public SingletonWithDependency singletonWithDependencyOne() {
			return new SingletonWithDependency( "first" );
		}

		// Must be defined as primary within the module context
		@Bean
		@Scope("prototype")
		@Primary
		public DependencyForSingleton firstDependencyForSingleton() {
			return new DependencyForSingleton( "first" );
		}
	}

	protected static class OtherConfig
	{
		@Bean
		public String otherBean() {
			return "otherBean";
		}

		@Bean
		@Scope("prototype")
		public SingletonWithDependency singletonWithDependencyTwo() {
			return new SingletonWithDependency( "other" );
		}

		@Bean
		@Scope("prototype")
		@Primary
		public DependencyForSingleton otherDependencyForSingleton() {
			return new DependencyForSingleton( "other" );
		}
	}

	protected static class SingletonWithDependency
	{
		private final String name;

		@Autowired
		private DependencyForSingleton dependencyForSingleton;

		public SingletonWithDependency( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getDependencyName() {
			return dependencyForSingleton.getName();
		}
	}

	protected static class DependencyForSingleton
	{
		private final String name;

		public DependencyForSingleton( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
