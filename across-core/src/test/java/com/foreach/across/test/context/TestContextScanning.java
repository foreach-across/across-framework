package com.foreach.across.test.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TestContextScanning
{
	@Test
	public void beansShouldBeReturnedInTheRegisteredOrderOfTheModules() {
		AcrossContext context = new AcrossContext();
		context.addModule( new ModuleOne() );
		context.addModule( new ModuleTwo() );
		context.addModule( new ModuleThree() );
		context.bootstrap();

		List<MyBeanConfig> beans = (List<MyBeanConfig>) AcrossContextUtils.getBeansOfType( context,
		                                                                                   MyBeanConfig.class,
		                                                                                   true );
		assertEquals( 3, beans.size() );

		assertEquals( "ModuleOne", beans.get( 0 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 1 ).getModule() );
		assertEquals( "ModuleThree", beans.get( 2 ).getModule() );

		context.shutdown();
	}

	@Test
	public void beansShouldBeReturnedInTheBootstrapOrderOfModules() {
		AcrossContext context = new AcrossContext();

		ModuleOne moduleOne = new ModuleOne();
		moduleOne.addRuntimeDependency( "ModuleThree" );
		context.addModule( moduleOne );

		ModuleTwo moduleTwo = new ModuleTwo();
		moduleTwo.addRuntimeDependency( "ModuleOne" );
		context.addModule( moduleTwo );

		ModuleThree moduleThree = new ModuleThree();
		context.addModule( moduleThree );

		context.bootstrap();

		List<MyBeanConfig> beans = (List<MyBeanConfig>) AcrossContextUtils.getBeansOfType( context,
		                                                                                   MyBeanConfig.class,
		                                                                                   true );
		assertEquals( 3, beans.size() );

		assertEquals( "ModuleThree", beans.get( 0 ).getModule() );
		assertEquals( "ModuleOne", beans.get( 1 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 2 ).getModule() );

		context.shutdown();
	}

	@Test
	public void beansFromTheParentContextArePositionedBeforeTheModuleBeans() {
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.getBeanFactory().registerSingleton( "", new MyFixedBeanConfig() );
		applicationContext.refresh();

		AcrossContext context = new AcrossContext( applicationContext );

		ModuleOne moduleOne = new ModuleOne();
		moduleOne.addRuntimeDependency( "ModuleTwo" );
		context.addModule( moduleOne );

		ModuleTwo moduleTwo = new ModuleTwo();
		moduleTwo.addRuntimeDependency( "ModuleThree" );
		context.addModule( moduleTwo );

		ModuleThree moduleThree = new ModuleThree();
		context.addModule( moduleThree );

		context.bootstrap();

		List<MyBeanConfig> beans = (List<MyBeanConfig>) AcrossContextUtils.getBeansOfType( context,
		                                                                                   MyBeanConfig.class,
		                                                                                   true );
		assertEquals( 4, beans.size() );

		assertEquals( "ApplicationContext", beans.get( 0 ).getModule() );
		assertEquals( "ModuleThree", beans.get( 1 ).getModule() );
		assertEquals( "ModuleTwo", beans.get( 2 ).getModule() );
		assertEquals( "ModuleOne", beans.get( 3 ).getModule() );

		context.shutdown();
		applicationContext.destroy();
	}

	@Configuration
	static class MyBeanConfig
	{
		@Autowired
		@Module(AcrossModule.CURRENT_MODULE)
		private AcrossModule module;

		public String getModule() {
			return module.getName();
		}
	}

	static class MyFixedBeanConfig extends MyBeanConfig
	{
		@Override
		public String getModule() {
			return "ApplicationContext";
		}
	}

	static class ModuleOne extends AcrossModule
	{
		@Override
		public String getName() {
			return "ModuleOne";
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new AnnotatedClassConfigurer( MyBeanConfig.class ) );
		}
	}

	static class ModuleTwo extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleTwo";
		}
	}

	static class ModuleThree extends ModuleOne
	{
		@Override
		public String getName() {
			return "ModuleThree";
		}
	}
}
