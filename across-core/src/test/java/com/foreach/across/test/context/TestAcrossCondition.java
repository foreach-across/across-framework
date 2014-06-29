package com.foreach.across.test.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestAcrossCondition
{
	private AcrossContext context;
	private AcrossModule moduleOne, moduleTwo, moduleThree, moduleFour;

	@Before
	public void prepareContextAndModules() {
		context = new AcrossContext();
		moduleOne = new EmptyAcrossModule( "moduleOne" );
		moduleTwo = new EmptyAcrossModule( "moduleTwo" );
		moduleThree = new EmptyAcrossModule( "moduleThree" );
		moduleFour = new EmptyAcrossModule( "moduleFour" );
	}

	@After
	public void cleanup() {
		context.shutdown();
	}

	@Test
	public void singleCondition() {
		context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( ConditionConfig.class ),
		                                         ConfigurerScope.MODULES_ONLY );
		context.addModule( moduleOne );
		context.addModule( moduleTwo );

		context.bootstrap();

		assertNotNull( conditionalBean( moduleOne ) );
		assertNull( conditionalBean( moduleTwo ) );
	}

	@Test
	public void notCreatedIfOneConditionFails() {
		context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( MultipleConditionConfig.class ),
		                                         ConfigurerScope.MODULES_ONLY );
		moduleOne.setProperty( "propertyOne", 123 );
		moduleOne.setProperty( "propertyTwo", 123 );
		context.addModule( moduleOne );

		moduleTwo.setProperty( "propertyOne", 123 );
		moduleTwo.setProperty( "propertyTwo", 456 );
		context.addModule( moduleTwo );

		moduleThree.setProperty( "propertyOne", 456 );
		moduleThree.setProperty( "propertyTwo", 456 );
		context.addModule( moduleThree );

		context.bootstrap();

		assertNotNull( conditionalBean( moduleOne ) );
		assertNull( conditionalBean( moduleTwo ) );
		assertNull( conditionalBean( moduleThree ) );
	}

	@Test
	public void notCreatedIfConditionsSucceedButDependencyFails() {
		context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( ConditionAndDependsConfig.class ),
		                                         ConfigurerScope.MODULES_ONLY );

		context.addModule( moduleOne );

		context.bootstrap();

		assertNull( conditionalBean( moduleOne ) );
	}

	@Test
	public void notCreatedIfDependencyMatchesButConditionsFail() {
		context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( ConditionAndDependsConfig.class ),
		                                         ConfigurerScope.MODULES_ONLY );

		context.addModule( moduleFour );

		context.bootstrap();

		assertNull( conditionalBean( moduleFour ) );
	}

	@Test
	public void createdIfBothDependencyMatchesAndConditionsSucceed() {
		context.addApplicationContextConfigurer( new AnnotatedClassConfigurer( ConditionAndDependsConfig.class ),
		                                         ConfigurerScope.MODULES_ONLY );

		context.addModule( moduleOne );
		context.addModule( moduleFour );

		context.bootstrap();

		assertNotNull( conditionalBean( moduleOne ) );
		assertNull( conditionalBean( moduleFour ) );
	}

	private Object conditionalBean( AcrossModule module ) {
		try {
			return AcrossContextUtils.getBeanFactory( module ).getBean( "conditionalBean" );
		}
		catch ( Exception e ) {
			return null;
		}
	}

	@Configuration
	@AcrossCondition("#{currentModule.name == 'moduleOne'}")
	static class ConditionConfig
	{
		@Bean
		public Object conditionalBean() {
			return "Bean created";
		}
	}

	@Configuration
	@AcrossCondition({ "${propertyOne} == ${propertyTwo}", "#{${propertyTwo} == 123}" })
	static class MultipleConditionConfig
	{
		@Bean
		public Object conditionalBean() {
			return "Bean created";
		}
	}

	@Configuration
	@AcrossDepends(required = "moduleFour")
	@AcrossCondition("#{currentModule.name == 'moduleOne'}")
	static class ConditionAndDependsConfig
	{
		@Bean
		public Object conditionalBean() {
			return "Bean created";
		}
	}
}