package com.foreach.across.test.spring;

import com.foreach.across.test.modules.exposing.LazyExposedBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.GenericApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test general approach of Spring ApplicationContext hierarchy and the way to expose beans
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
		parentProcessor = new CustomPostProcessor();
		parent.getBeanFactory().addBeanPostProcessor( parentProcessor );
		parent.refresh();

		childOne = new AnnotationConfigApplicationContext();
		childOne.register( Config.class );
		childOneProcessor = new CustomPostProcessor();
		childOne.getBeanFactory().addBeanPostProcessor( childOneProcessor );
		childOne.setParent( parent );
		childOne.refresh();

		Factory factory = new Factory( childOne );
		parent.getBeanFactory().registerSingleton( "factory", factory );

		childTwo = new AnnotationConfigApplicationContext();
		childTwo.register( OtherConfig.class );
		childTwoProcessor = new CustomPostProcessor();
		childTwo.getBeanFactory().addBeanPostProcessor( childTwoProcessor );
		childTwo.setParent( parent );
		childTwo.refresh();
	}

	@After
	public void destroyContext() {
		childOne.destroy();
		childTwo.destroy();
		parent.destroy();
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
		registerBeanInParent( "myBean" );

		assertEquals( "myBean", getBean( parent, "myBean" ) );
		assertEquals( "myBean", getBean( childTwo, "myBean" ) );

		assertTrue( parent.containsBean( "myBean" ) );
		assertTrue( childTwo.containsBean( "myBean" ) );
	}

	@Test
	public void postProcessorShouldNotRunInParentContext() {
		registerBeanInParent( "myBean" );

		assertEquals( "myBean", getBean( parent, "myBean" ) );

		assertTrue( childOneProcessor.handled( "myBean" ) );
		assertFalse( childTwoProcessor.handled( "myBean" ) );
		assertFalse( parentProcessor.handled( "myBean" ) );
	}

	@Test
	public void prototypeAlwaysGetsCreated() {
		registerBeanInParent( "prototypeBean" );

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
		registerBeanInParent( "lazyBean" );

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

	private void registerBeanInParent( String beanName ) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition( LazyExposedBean.class );
		beanDefinition.setTargetType( LazyExposedBean.class );
		beanDefinition.setBeanClass( LazyExposedBean.class );
		beanDefinition.setBeanClassName( LazyExposedBean.class.getName() );
		beanDefinition.setFactoryBeanName( "factory" );
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
	}

	protected static class OtherConfig
	{
		@Bean
		public String otherBean() {
			return "otherBean";
		}
	}
}
