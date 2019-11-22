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
package performance;

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.across.test.support.AcrossTestBuilders;
import com.foreach.across.test.support.AcrossTestContextBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
class TestApplicationContextStartup
{
	private static final int WARMUP_RUNS = 2;

	/**
	 * Number of times an application should be started for the timings test.
	 */
	private static final int TIMES_TO_RUN = 5;

	/**
	 * Number of child contexts or modules that should be created.
	 */
	private static final int NUMBER_OF_CHILD_CONTEXTS = 30;

	/**
	 * Number of beans that a single child context should have (when working with child contexts).
	 */
	private static final int BEANS_PER_CHILD_CONTEXT = 400;

	/**
	 * Number of the beans that should be exposed by a single child context.
	 */
	private static final int EXPOSED_PER_CHILD_CONTEXT = 20;

	/**
	 * Total number of beans that should be registered and created.
	 */
	private static final int TOTAL_BEANS = NUMBER_OF_CHILD_CONTEXTS * BEANS_PER_CHILD_CONTEXT;

	@Test
	@DisplayName("Compare ApplicationContext startup times")
	void calculateStartupTimes() {
		PrintStream out = System.out;
		System.out.println( "Executing " + TIMES_TO_RUN + " times each - warmup of " + WARMUP_RUNS );
		System.out.println( "Temporarily disabling system output." );
		System.setOut( null );

		try {
			out.println( "Single AnnotationConfigApplicationContext" );
			BigDecimal singleAnnotationConfigApplicationContext = calculateAverage( () -> singleApplicationContext( AnnotationConfigApplicationContext::new ) );

			out.println( "Single AcrossApplicationContext" );
			BigDecimal singleAcrossApplicationContext = calculateAverage( () -> singleApplicationContext( AcrossApplicationContext::new ) );

			// out.println( "Single AnnotationConfigWebApplicationContext" );
			// BigDecimal singleAnnotationConfigWebApplicationContext
			// 		= calculateAverage( () -> singleApplicationContext( AnnotationConfigWebApplicationContext::new ) );

			out.println( "Multiple AnnotationConfigApplicationContext" );
			BigDecimal multipleAnnotationConfigApplicationContexts
					= calculateAverage( () -> multipleApplicationContextsWithParent( AnnotationConfigApplicationContext::new ) );

			out.println( "Multiple AcrossApplicationContext" );
			BigDecimal multipleAcrossApplicationContexts
					= calculateAverage( () -> multipleApplicationContextsWithParent( AcrossApplicationContext::new ) );

			out.println( "AcrossContext without exposed" );
			BigDecimal acrossContextWithoutExposed = calculateAverage( () -> acrossContext( false ) );

			out.println( "AcrossContext with exposed" );
			BigDecimal acrossContextWithExposed = calculateAverage( () -> acrossContext( true ) );

			System.setOut( out );

			System.out.println();
			System.out.println( "Performance timing results - average of " + TIMES_TO_RUN + " times each:" );
			System.out.println(
					TOTAL_BEANS + " beans in total, " + NUMBER_OF_CHILD_CONTEXTS + " child contexts, " + BEANS_PER_CHILD_CONTEXT + " beans per context, " + EXPOSED_PER_CHILD_CONTEXT + " exposed" );
			System.out.println();
			System.out.println( "+----------------------------------------------------+------+" );

			String pattern = "| %-50s | %4.0f |";
			System.out.println( String.format( pattern, "Single AnnotationConfigApplicationContext", singleAnnotationConfigApplicationContext ) );
			System.out.println( String.format( pattern, "Single AcrossApplicationContext", singleAcrossApplicationContext ) );
			//System.out.println( String.format( pattern, "Single AnnotationConfigWebApplicationContext", singleAnnotationConfigWebApplicationContext ) );
			System.out.println( String.format( pattern, "Multiple AnnotationConfigApplicationContext", multipleAnnotationConfigApplicationContexts ) );
			System.out.println( String.format( pattern, "Multiple AcrossApplicationContext", multipleAcrossApplicationContexts ) );
			System.out.println( String.format( pattern, "AcrossContext without exposed beans", acrossContextWithoutExposed ) );
			System.out.println( String.format( pattern, "AcrossContext with exposed beans", acrossContextWithExposed ) );
			System.out.println( "+----------------------------------------------------+------+" );
		}
		finally {
			// make sure output is set back
			System.setOut( out );
		}
	}

	@Test
	@DisplayName("DefaultAcrossContextBeanRegistry getBeansOfType performance")
	void calculateBeansOfTypePerformance() {
		AcrossTestContextBuilder testContextBuilder = AcrossTestBuilders.standard( false );

		IntStream.range( 0, NUMBER_OF_CHILD_CONTEXTS )
		         .forEach( i -> {
			         EmptyAcrossModule module = new EmptyAcrossModule( "Module" + i, SimpleContextConfiguration.class );
			         module.expose( MyBean.class );
			         testContextBuilder.modules( module );
		         } );

		int testCount = 10;
		long exposedOnly, moduleInternals, nonExisting;

		PrintStream out = System.out;

		try {
			System.setOut( null );

			try (AcrossTestContext ctx = testContextBuilder.build()) {
				exposedOnly = timeStartup( () -> IntStream.range( 0, testCount )
				                                          .forEach( i -> assertThat( ctx.getBeansOfType( MyBean.class, false ) )
						                                          .hasSize( TOTAL_BEANS ) ) );
				moduleInternals = timeStartup( () -> IntStream.range( 0, testCount )
				                                              .forEach( i -> assertThat( ctx.getBeansOfType( MyBean.class, true ) )
						                                              .hasSize( TOTAL_BEANS ) ) );

				nonExisting = timeStartup( () -> IntStream.range( 0, testCount )
				                                          .forEach( i -> assertThat( ctx.getBeansOfType( String.class, true ) ).isEmpty() ) );
			}
		}
		finally {
			// make sure output is set back
			System.setOut( out );
		}

		System.out.println();
		System.out.println( "Performance timing results - average of " + testCount + " times each:" );
		System.out.println(
				TOTAL_BEANS + " beans in total, " + NUMBER_OF_CHILD_CONTEXTS + " child contexts, " + BEANS_PER_CHILD_CONTEXT + " beans per context" );
		System.out.println();
		System.out.println( "+----------------------------------------------------+------+" );

		String pattern = "| %-50s | %4.0f |";
		System.out.println( String.format( pattern, "Only exposed beans", BigDecimal.valueOf( exposedOnly ) ) );
		System.out.println( String.format( pattern, "With module internals", BigDecimal.valueOf( moduleInternals ) ) );
		System.out.println( String.format( pattern, "Non-existing module internals", BigDecimal.valueOf( nonExisting ) ) );
		System.out.println( "+----------------------------------------------------+------+" );
	}

	@Test
	@DisplayName("RefreshableCollection performance")
	void refreshableCollectionPerformance() {
		PrintStream out = System.out;
		System.out.println( "Executing " + TIMES_TO_RUN + " times each - warmup of " + WARMUP_RUNS );
		System.out.println( "Temporarily disabling system output." );
		System.setOut( null );

		try {
			out.println( "Simple RefreshableCollection" );
			BigDecimal simpleRefreshableCollection = calculateAverage( () -> acrossContext( WithRefreshableCollection.class ) );

			out.println( "Incremental RefreshableCollection" );
			BigDecimal incrementalRefreshableCollection = calculateAverage( () -> acrossContext( WithIncrementalRefreshableCollection.class ) );

			out.println( "RefreshableCollection with internals" );
			BigDecimal simpleWithInternals = calculateAverage( () -> acrossContext( WithInternalsRefreshableCollection.class ) );

			out.println( "Incremental RefreshableCollection with internals" );
			BigDecimal incrementalWithInternals = calculateAverage( () -> acrossContext( WithIncrementalRefreshableCollection.class ) );

			System.setOut( out );

			System.out.println();
			System.out.println( "Performance timing results - average of " + TIMES_TO_RUN + " times each:" );
			System.out.println(
					TOTAL_BEANS + " beans in total, " + NUMBER_OF_CHILD_CONTEXTS + " child contexts, " + BEANS_PER_CHILD_CONTEXT + " beans per context, " + EXPOSED_PER_CHILD_CONTEXT + " exposed" );
			System.out.println();
			System.out.println( "+----------------------------------------------------+------+" );

			String pattern = "| %-50s | %4.0f |";
			System.out.println( String.format( pattern, "Simple RefreshableCollection", simpleRefreshableCollection ) );
			System.out.println( String.format( pattern, "Incremental RefreshableCollection", incrementalRefreshableCollection ) );
			System.out.println( String.format( pattern, "RefreshableCollection with internals", simpleWithInternals ) );
			System.out.println( String.format( pattern, "Incremental RefreshableCollection with internals", incrementalWithInternals ) );
			System.out.println( "+----------------------------------------------------+------+" );
		}
		finally {
			// make sure output is set back
			System.setOut( out );
		}
	}

	void acrossContext( boolean exposeBeans ) {
		AcrossTestContextBuilder testContextBuilder = AcrossTestBuilders.standard( false );

		IntStream.range( 0, NUMBER_OF_CHILD_CONTEXTS )
		         .forEach( i -> {
			         EmptyAcrossModule module = new EmptyAcrossModule( "Module" + i, SimpleContextConfiguration.class );
			         if ( exposeBeans ) {
				         module.expose( ExposedBean.class );
			         }
			         testContextBuilder.modules( module );
		         } );

		try (AcrossTestContext ctx = testContextBuilder.build()) {
			assertThat(
					ctx.getContextInfo().getModules()
					   .stream()
					   .filter( AcrossModuleInfo::isBootstrapped )
					   .map( AcrossModuleInfo::getApplicationContext )
					   .mapToInt( applicationContext -> applicationContext.getBeansOfType( MyBean.class ).size() )
					   .sum()
			).isEqualTo( TOTAL_BEANS );
			assertThat( ctx.getBeansOfType( MyBean.class, true ) ).hasSize( TOTAL_BEANS );
			assertThat( ctx.getBeansOfType( MyBean.class ) ).hasSize( exposeBeans ? EXPOSED_PER_CHILD_CONTEXT * NUMBER_OF_CHILD_CONTEXTS : 0 );
		}
	}

	void acrossContext( Class additionalConfiguration ) {
		AcrossTestContextBuilder testContextBuilder = AcrossTestBuilders.standard( false );

		IntStream.range( 0, NUMBER_OF_CHILD_CONTEXTS )
		         .forEach( i -> {
			         EmptyAcrossModule module = new EmptyAcrossModule( "Module" + i, SimpleContextConfiguration.class, additionalConfiguration );
			         module.expose( ExposedBean.class );
			         testContextBuilder.modules( module );
		         } );

		try (AcrossTestContext ctx = testContextBuilder.build()) {
			assertThat(
					ctx.getContextInfo().getModules()
					   .stream()
					   .filter( AcrossModuleInfo::isBootstrapped )
					   .map( AcrossModuleInfo::getApplicationContext )
					   .mapToInt( applicationContext -> applicationContext.getBeansOfType( MyBean.class ).size() )
					   .sum()
			).isEqualTo( TOTAL_BEANS );
		}
	}

	private <U extends ConfigurableApplicationContext & AnnotationConfigRegistry> void singleApplicationContext( Supplier<U> supplier ) {
		ConfigurableApplicationContext applicationContext = supplier.get();
		try {
			( (AnnotationConfigRegistry) applicationContext ).register( SimpleContextConfiguration.class );
			applicationContext.refresh();
			applicationContext.start();
			assertThat( applicationContext.getBeansOfType( MyBean.class ) ).hasSize( TOTAL_BEANS );
		}
		finally {
			applicationContext.stop();
		}
	}

	private <U extends ConfigurableApplicationContext & AnnotationConfigRegistry> void multipleApplicationContextsWithParent( Supplier<U> supplier ) {
		List<ConfigurableApplicationContext> createdContexts = new ArrayList<>( 51 );
		ConfigurableApplicationContext parentContext = supplier.get();
		parentContext.refresh();
		parentContext.start();

		IntStream.range( 0, NUMBER_OF_CHILD_CONTEXTS )
		         .forEach( i -> {
			         ConfigurableApplicationContext applicationContext = supplier.get();
			         applicationContext.setParent( parentContext );
			         ( (AnnotationConfigRegistry) applicationContext ).register( SimpleContextConfiguration.class );
			         applicationContext.refresh();
			         applicationContext.start();
			         createdContexts.add( applicationContext );
		         } );

		createdContexts.forEach( a -> assertThat( a.getBeansOfType( MyBean.class ) ).hasSize( BEANS_PER_CHILD_CONTEXT ) );

		createdContexts.add( parentContext );
		createdContexts.forEach( ConfigurableApplicationContext::close );
	}

	private BigDecimal calculateAverage( Runnable runnable ) {
		for ( int i = 0; i < WARMUP_RUNS; i++ ) {
			timeStartup( runnable );
		}

		BigDecimal total = new BigDecimal( 0 );

		for ( int i = 0; i < TIMES_TO_RUN; i++ ) {
			total = total.add( BigDecimal.valueOf( timeStartup( runnable ) ) );
		}

		return total.divide( BigDecimal.valueOf( TIMES_TO_RUN ), RoundingMode.HALF_UP );
	}

	@SneakyThrows
	private long timeStartup( Runnable runnable ) {
		AtomicLong l = new AtomicLong();
		Thread thread = new Thread(
				() -> {
					StopWatch sw = new StopWatch();
					sw.start();
					runnable.run();
					sw.stop();

					l.set( sw.getTime() );
				}
		);
		thread.start();
		thread.join();

		return l.get();
	}

	interface MyBean
	{
	}

	static class SimpleContextConfiguration implements ApplicationContextAware
	{
		@Override
		public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
			if ( applicationContext instanceof AnnotationConfigWebApplicationContext ) {
				// AnnotationConfigWebApplicationContext appCtx = (AnnotationConfigWebApplicationContext) applicationContext;
				// IntStream.range( 0, applicationContext.getParent() != null ? BEANS_PER_CHILD_CONTEXT : TOTAL_BEANS )
				//          .forEach( i -> appCtx.beanDef( "registeredBean" + i, MyBean.class ) );
			}
			else {
				AnnotationConfigApplicationContext appCtx = (AnnotationConfigApplicationContext) applicationContext;
				int nonExposedBeans = applicationContext.getParent() != null
						? BEANS_PER_CHILD_CONTEXT - EXPOSED_PER_CHILD_CONTEXT
						: TOTAL_BEANS - ( NUMBER_OF_CHILD_CONTEXTS * EXPOSED_PER_CHILD_CONTEXT );
				int exposedBeans = applicationContext.getParent() != null ? EXPOSED_PER_CHILD_CONTEXT : NUMBER_OF_CHILD_CONTEXTS * EXPOSED_PER_CHILD_CONTEXT;
				IntStream.range( 0, nonExposedBeans )
				         .forEach( i -> appCtx.registerBean( "nonExposedBean" + i, NonExposedBean.class ) );
				IntStream.range( 0, exposedBeans )
				         .forEach( i -> appCtx.registerBean( "exposedBean" + i, ExposedBean.class ) );
			}
		}
	}

	@Component
	static class WithRefreshableCollection
	{
		@RefreshableCollection
		private Collection<ExposedBean> exposedBeans;
	}

	@Component
	static class WithIncrementalRefreshableCollection
	{
		@RefreshableCollection(incremental = true)
		private Collection<ExposedBean> exposedBeans;
	}

	@Component
	static class WithInternalsRefreshableCollection
	{
		@RefreshableCollection(includeModuleInternals = true)
		private Collection<ExposedBean> exposedBeans;
	}

	@Component
	static class WithInternalsIncrementalRefreshableCollection
	{
		@RefreshableCollection(includeModuleInternals = true, incremental = true)
		private Collection<ExposedBean> exposedBeans;
	}

	static class NonExposedBean implements MyBean
	{
	}

	static class ExposedBean implements MyBean
	{

	}
}
