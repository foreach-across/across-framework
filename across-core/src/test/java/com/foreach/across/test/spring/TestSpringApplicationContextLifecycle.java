package com.foreach.across.test.spring;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.installers.InstallerAction;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

import javax.sql.DataSource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestSpringApplicationContextLifecycle
{
	@Test
	public void destroySpringContextWithoutChild() {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext( Config.class );
		ConfigurableListableBeanFactory beanFactory = parent.getBeanFactory();

		assertNotNull( beanFactory.getSingleton( "myBean" ) );

		parent.destroy();
		assertNull( beanFactory.getSingleton( "myBean" ) );
	}

	@Test
	public void destroySpringContextWithChild() {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext( Config.class );
		ConfigurableListableBeanFactory parentFactory = parent.getBeanFactory();

		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext( Config.class );
		child.setParent( parent );
		ConfigurableListableBeanFactory childFactory = child.getBeanFactory();

		assertNotNull( parentFactory.getSingleton( "myBean" ) );
		assertNotNull( childFactory.getSingleton( "myBean" ) );
		assertNotSame( parentFactory.getSingleton( "myBean" ), childFactory.getSingleton( "myBean" ) );

		parent.destroy();
		assertNull( parentFactory.getSingleton( "myBean" ) );
		assertNotNull( childFactory.getSingleton( "myBean" ) );
	}

	@Test
	public void destroySpringChildContext() {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext( Config.class );
		ConfigurableListableBeanFactory parentFactory = parent.getBeanFactory();

		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext( Config.class );
		child.setParent( parent );
		ConfigurableListableBeanFactory childFactory = child.getBeanFactory();

		assertNotNull( parentFactory.getSingleton( "myBean" ) );
		assertNotNull( childFactory.getSingleton( "myBean" ) );
		assertNotSame( parentFactory.getSingleton( "myBean" ), childFactory.getSingleton( "myBean" ) );

		child.destroy();
		assertNotNull( parentFactory.getSingleton( "myBean" ) );
		assertNull( childFactory.getSingleton( "myBean" ) );
	}

	@Test
	public void destroyAcrossContextDirectly() {
		AcrossContext across = new AcrossContext();
		across.setInstallerAction( InstallerAction.DISABLED );
		across.setDataSource( mock( DataSource.class ) );

		AcrossModule moduleOne = new EmptyAcrossModule( "moduleOne" );
		moduleOne.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		AcrossModule moduleTwo = new EmptyAcrossModule( "moduleTwo" );
		moduleTwo.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		across.addModule( moduleOne );
		across.addModule( moduleTwo );

		across.bootstrap();

		AbstractApplicationContext acrossApplicationContext = AcrossContextUtils.getApplicationContext( across );
		ConfigurableListableBeanFactory acrossFactory = AcrossContextUtils.getBeanFactory( across );
		ConfigurableListableBeanFactory moduleOneFactory = AcrossContextUtils.getBeanFactory( moduleOne );
		ConfigurableListableBeanFactory moduleTwoFactory = AcrossContextUtils.getBeanFactory( moduleTwo );

		assertTrue( acrossApplicationContext.isActive() );
		assertNotNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNotNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNotNull( moduleTwoFactory.getSingleton( "myBean" ) );
		assertNotSame( moduleOneFactory.getSingleton( "myBean" ), moduleTwoFactory.getSingleton( "myBean" ) );

		across.shutdown();

		// TODO: fixme see also #38
		//assertFalse( acrossApplicationContext.isActive() );
		//assertNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNull( moduleTwoFactory.getSingleton( "myBean" ) );
	}

	@Test
	public void destroyingParentContextHasNoEffectIfAcrossContextBeanNotPresent() {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext( Config.class );

		AcrossContext across = new AcrossContext( parent );
		across.setInstallerAction( InstallerAction.DISABLED );
		across.setDataSource( mock( DataSource.class ) );

		AcrossModule moduleOne = new EmptyAcrossModule( "moduleOne" );
		moduleOne.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		AcrossModule moduleTwo = new EmptyAcrossModule( "moduleTwo" );
		moduleTwo.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		across.addModule( moduleOne );
		across.addModule( moduleTwo );

		across.bootstrap();

		AbstractApplicationContext acrossApplicationContext = AcrossContextUtils.getApplicationContext( across );
		ConfigurableListableBeanFactory acrossFactory = AcrossContextUtils.getBeanFactory( across );
		ConfigurableListableBeanFactory moduleOneFactory = AcrossContextUtils.getBeanFactory( moduleOne );
		ConfigurableListableBeanFactory moduleTwoFactory = AcrossContextUtils.getBeanFactory( moduleTwo );

		assertTrue( acrossApplicationContext.isActive() );
		assertEquals( parent, acrossApplicationContext.getParent() );
		assertNotNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNotNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNotNull( moduleTwoFactory.getSingleton( "myBean" ) );
		assertNotSame( moduleOneFactory.getSingleton( "myBean" ), moduleTwoFactory.getSingleton( "myBean" ) );

		parent.close();

		assertTrue( acrossApplicationContext.isActive() );
		assertEquals( parent, acrossApplicationContext.getParent() );
		assertNotNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNotNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNotNull( moduleTwoFactory.getSingleton( "myBean" ) );
		assertNotSame( moduleOneFactory.getSingleton( "myBean" ), moduleTwoFactory.getSingleton( "myBean" ) );
	}

	@Test
	public void destroyParentContextWithAcrossAsSingleton() {
		AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext( Config.class );

		AcrossContext across = new AcrossContext( parent );
		across.setInstallerAction( InstallerAction.DISABLED );
		across.setDataSource( mock( DataSource.class ) );

		// AcrossContext configuration is bean in the parent and should be destroyed
		( (DefaultListableBeanFactory) parent.getBeanFactory() ).registerDisposableBean( "acrossContext", across );

		AcrossModule moduleOne = new EmptyAcrossModule( "moduleOne" );
		moduleOne.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		AcrossModule moduleTwo = new EmptyAcrossModule( "moduleTwo" );
		moduleTwo.addApplicationContextConfigurer( new AnnotatedClassConfigurer( Config.class ) );

		across.addModule( moduleOne );
		across.addModule( moduleTwo );

		across.bootstrap();

		AbstractApplicationContext acrossApplicationContext = AcrossContextUtils.getApplicationContext( across );
		ConfigurableListableBeanFactory acrossFactory = AcrossContextUtils.getBeanFactory( across );
		ConfigurableListableBeanFactory moduleOneFactory = AcrossContextUtils.getBeanFactory( moduleOne );
		ConfigurableListableBeanFactory moduleTwoFactory = AcrossContextUtils.getBeanFactory( moduleTwo );

		assertTrue( acrossApplicationContext.isActive() );
		assertEquals( parent, acrossApplicationContext.getParent() );
		assertNotNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNotNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNotNull( moduleTwoFactory.getSingleton( "myBean" ) );
		assertNotSame( moduleOneFactory.getSingleton( "myBean" ), moduleTwoFactory.getSingleton( "myBean" ) );

		parent.close();

		// TODO: fixme see also #38
		//assertFalse( acrossApplicationContext.isActive() );
		//assertNull( acrossFactory.getSingleton( AcrossContextInfo.BEAN ) );
		assertNull( moduleOneFactory.getSingleton( "myBean" ) );
		assertNull( moduleTwoFactory.getSingleton( "myBean" ) );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public Object myBean() {
			return new String( "bean" );
		}
	}
}
