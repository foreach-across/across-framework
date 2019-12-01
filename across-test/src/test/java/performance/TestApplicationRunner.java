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

import com.foreach.across.core.context.AcrossListableBeanFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import performance.applications.PerformanceTestApplications;
import performance.applications.simple.application.ExposedComponent;
import performance.applications.simple.application.InternalComponent;
import performance.applications.simpleweb.application.SimpleController;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
class TestApplicationRunner
{
	private static final int WARMUP_RUNS = 1;

	/**
	 * Number of times an application should be started for the timings test.
	 */
	private static final int TIMES_TO_RUN = 4;

	@Test
	@DisplayName("Verify simple Spring Boot: all components available")
	void verifySimpleBoot() {
		ConfigurableApplicationContext context = PerformanceTestApplications.springBootSimple();
		assertThat( context.getBean( ExposedComponent.class ) ).isNotNull();
		assertThat( context.containsBean( "internalComponent" ) ).isTrue();
		assertThat( context.getBean( InternalComponent.class ) ).isNotNull();
		context.close();
	}

	@Test
	@DisplayName("Verify simple Across as Spring Boot: only exposed component")
	void verifySimpleAcrossAsBoot() {
		ConfigurableApplicationContext context = PerformanceTestApplications.acrossAsSpringBootApplicationSimple();
		assertThat( context.getBean( ExposedComponent.class ) ).isNotNull();
		assertThat( context.containsBean( "internalComponent" ) ).isFalse();
		context.close();
	}

	@Test
	@DisplayName("Verify simple web Spring Boot: all components available")
	@SneakyThrows
	void verifySimpleWebBoot() {
		ConfigurableWebApplicationContext context = PerformanceTestApplications.springBootSimpleWeb();
		assertThat( context.containsBean( "simpleController" ) ).isTrue();
		assertThat( context.getBean( SimpleController.class ) ).isNotNull();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup( context ).build();
		mockMvc.perform( get( "/" ) ).andExpect( content().string( "hello" ) );

		context.close();
	}

	@Test
	@DisplayName("Verify simple web Across as Spring Boot: controllers are exposed")
	@SneakyThrows
	void verifySimpleWebAcrossAsBoot() {
		ConfigurableWebApplicationContext context = PerformanceTestApplications.acrossAsSpringBootApplicationSimpleWeb();
		assertThat( context.containsBean( "simpleController" ) ).isTrue();
		assertThat( context.getBean( SimpleController.class ) ).isNotNull();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup( context ).build();
		mockMvc.perform( get( "/" ) ).andExpect( content().string( "hello" ) );

		assertThat( context.getBeanFactory().getParentBeanFactory() ).isInstanceOf( AcrossListableBeanFactory.class );
		context.close();
	}

	@Test
	@DisplayName("Verify simple web Across using AcrossApplicationRunner: no parent beanfactory")
	@SneakyThrows
	void verifySimpleWebAcross() {
		ConfigurableWebApplicationContext context = PerformanceTestApplications.acrossApplicationSimpleWeb();
		assertThat( context.containsBean( "simpleController" ) ).isTrue();
		assertThat( context.getBean( SimpleController.class ) ).isNotNull();

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup( context ).build();
		mockMvc.perform( get( "/" ) ).andExpect( content().string( "hello" ) );

		assertThat( context.getBeanFactory().getParentBeanFactory() ).isNull();
		context.close();
	}

	@Test
	@DisplayName("Compare Boot and Across startup times")
	void calculateStartupTimes() {
		PrintStream out = System.out;
		System.out.println( "Executing " + TIMES_TO_RUN + " times each - warmup of " + WARMUP_RUNS );
		System.out.println( "Temporarily disabling system output." );
		System.setOut( null );

		try {
			out.println( "Simple: regular SpringBootApplication" );
			BigDecimal simpleBootAvg = calculateAverage( PerformanceTestApplications::springBootSimple );

			out.println( "Simple: Boot using AcrossApplicationRunner" );
			BigDecimal simpleBootAsAcrossApp = calculateAverage( PerformanceTestApplications::springBootSimpleAsAcrossApplication );

			out.println( "Simple: Across as SpringBootApplication" );
			BigDecimal simpleAcrossAsBootAvg = calculateAverage( PerformanceTestApplications::acrossAsSpringBootApplicationSimple );

			out.println( "Simple: Across using AcrossApplicationRunner" );
			BigDecimal simpleAcrossAvg = calculateAverage( PerformanceTestApplications::acrossApplicationSimple );

			out.println( "Simple Web: regular SpringBootApplication" );
			BigDecimal simpleWebBootAvg = calculateAverage( PerformanceTestApplications::springBootSimpleWeb );

			out.println( "Simple Web: Boot using AcrossApplicationRunner" );
			BigDecimal simpleWebBootAsAcrossApp = calculateAverage( PerformanceTestApplications::springBootSimpleWebAsAcrossApplication );

			out.println( "Simple Web: Across as SpringBootApplication" );
			BigDecimal simpleWebAcrossAsBootAvg = calculateAverage( PerformanceTestApplications::acrossAsSpringBootApplicationSimpleWeb );

			out.println( "Simple Web: Across using AcrossApplicationRunner" );
			BigDecimal simpleWebAcrossAvg = calculateAverage( PerformanceTestApplications::acrossApplicationSimpleWeb );

			System.setOut( out );

			System.out.println();
			System.out.println( "Performance timing results - average of " + TIMES_TO_RUN + " times each:" );
			System.out.println();
			System.out.println(
					"+--------------------------+------+--------------------------------+------------------------+------------------------------+" );
			System.out.println(
					"| Application              | Boot | Boot + AcrossApplicationRunner | AX + SpringApplication | AX + AcrossApplicationRunner |" );
			System.out.println(
					"+--------------------------+------+--------------------------------+------------------------+------------------------------+" );

			String pattern = "| %-24s | %4.0f | %30.0f | %22.0f | %28.0f |";
			System.out.println( String.format( pattern, "Simple", simpleBootAvg, simpleBootAsAcrossApp, simpleAcrossAsBootAvg, simpleAcrossAvg ) );
			System.out.println(
					String.format( pattern, "Simple Web", simpleWebBootAvg, simpleWebBootAsAcrossApp, simpleWebAcrossAsBootAvg, simpleWebAcrossAvg ) );
			System.out.println(
					"+--------------------------+------+--------------------------------+------------------------+------------------------------+" );
		}
		finally {
			// make sure output is set back
			System.setOut( out );
		}
	}

	static BigDecimal calculateAverage( Supplier<ConfigurableApplicationContext> supplier ) {
		for ( int i = 0; i < WARMUP_RUNS; i++ ) {
			timeStartup( supplier );
		}

		BigDecimal total = new BigDecimal( 0 );

		for ( int i = 0; i < TIMES_TO_RUN; i++ ) {
			total = total.add( BigDecimal.valueOf( timeStartup( supplier ) ) );
		}

		return total.divide( BigDecimal.valueOf( TIMES_TO_RUN ), RoundingMode.HALF_UP );
	}

	@SneakyThrows
	static long timeStartup( Supplier<ConfigurableApplicationContext> supplier ) {
		AtomicLong l = new AtomicLong();
		Thread thread = new Thread(
				() -> {
					StopWatch sw = new StopWatch();
					sw.start();
					ConfigurableApplicationContext context = supplier.get();
					sw.stop();
					context.close();

					l.set( sw.getTime() );
				}
		);
		thread.start();
		thread.join();

		return l.get();
	}
}
