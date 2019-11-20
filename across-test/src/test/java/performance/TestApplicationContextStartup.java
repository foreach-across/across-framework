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
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
	private static final int TIMES_TO_RUN = 20;

	/**
	 * Total number of beans that should be registered and created.
	 */
	private static final int TOTAL_BEANS = 1000;

	/**
	 * Number of child contexts or modules that should be created.
	 */
	private static final int NUMBER_OF_CHILD_CONTEXTS = 20;

	/**
	 * Number of beans that a single child context should have (when working with child contexts).
	 */
	private static final int BEANS_PER_CHILD_CONTEXT = TOTAL_BEANS / NUMBER_OF_CHILD_CONTEXTS;

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
			//
			// out.println( "Simple: Across using AcrossApplicationRunner" );
			// BigDecimal simpleAcrossAvg = calculateAverage( PerformanceTestApplications::acrossApplicationSimple );
			//
			// out.println( "Simple Web: regular SpringBootApplication" );
			// BigDecimal simpleWebBootAvg = calculateAverage( PerformanceTestApplications::springBootSimpleWeb );
			//
			// out.println( "Simple Web: Boot using AcrossApplicationRunner" );
			// BigDecimal simpleWebBootAsAcrossApp = calculateAverage( PerformanceTestApplications::springBootSimpleWebAsAcrossApplication );
			//
			// out.println( "Simple Web: Across as SpringBootApplication" );
			// BigDecimal simpleWebAcrossAsBootAvg = calculateAverage( PerformanceTestApplications::acrossAsSpringBootApplicationSimpleWeb );
			//
			// out.println( "Simple Web: Across using AcrossApplicationRunner" );
			// BigDecimal simpleWebAcrossAvg = calculateAverage( PerformanceTestApplications::acrossApplicationSimpleWeb );

			System.setOut( out );

			System.out.println();
			System.out.println( "Performance timing results - average of " + TIMES_TO_RUN + " times each:" );
			System.out.println(
					TOTAL_BEANS + " beans in total, " + NUMBER_OF_CHILD_CONTEXTS + " child contexts, " + BEANS_PER_CHILD_CONTEXT + " beans per context" );
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
			// System.out.println( String.format( pattern, "Simple", simpleBootAvg, simpleBootAsAcrossApp, simpleAcrossAsBootAvg, simpleAcrossAvg ) );
			// System.out.println(
			// 		String.format( pattern, "Simple Web", simpleWebBootAvg, simpleWebBootAsAcrossApp, simpleWebAcrossAsBootAvg, simpleWebAcrossAvg ) );
			System.out.println( "+----------------------------------------------------+------+" );
		}
		finally {
			// make sure output is set back
			System.setOut( out );
		}
	}

	void acrossContext( boolean exposed ) {
		AcrossTestContextBuilder testContextBuilder = AcrossTestBuilders.standard( false );

		IntStream.range( 0, NUMBER_OF_CHILD_CONTEXTS )
		         .forEach( i -> {
			         EmptyAcrossModule module = new EmptyAcrossModule( "Module" + i, SmallBeansRegistrar.class );
			         if ( exposed ) {
				         module.expose( MyBean.class );
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
			assertThat( ctx.getBeansOfType( MyBean.class ) ).hasSize( exposed ? TOTAL_BEANS : 0 );
		}
	}

	private <U extends ConfigurableApplicationContext & AnnotationConfigRegistry> void singleApplicationContext( Supplier<U> supplier ) {
		ConfigurableApplicationContext applicationContext = supplier.get();
		try {
			( (AnnotationConfigRegistry) applicationContext ).register( SmallBeansRegistrar.class );
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
			         ( (AnnotationConfigRegistry) applicationContext ).register( SmallBeansRegistrar.class );
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

	static class SmallBeansRegistrar implements ApplicationContextAware
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
				IntStream.range( 0, applicationContext.getParent() != null ? BEANS_PER_CHILD_CONTEXT : TOTAL_BEANS )
				         .forEach( i -> appCtx.registerBean( "registeredBean" + i, MyBean.class ) );
			}
		}
	}

	static class MyBean
	{

	}
}
