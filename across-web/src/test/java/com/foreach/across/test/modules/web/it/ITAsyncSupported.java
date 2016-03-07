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
package com.foreach.across.test.modules.web.it;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.test.modules.web.it.modules.async.AsyncModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@ContextConfiguration(classes = ITAsyncSupported.Config.class)
public class ITAsyncSupported extends AbstractWebIntegrationTest
{
	@Autowired
	private AsyncTaskExecutor spyTaskExecutor;

	@Autowired
	private CallableProcessingInterceptor callableProcessingInterceptor;

	@Autowired
	private DeferredResultProcessingInterceptor deferredResultProcessingInterceptor;

	@Before
	public void resetMocks() {
		reset( callableProcessingInterceptor, deferredResultProcessingInterceptor, spyTaskExecutor );
	}

	@Test
	public void callableShouldWork() throws Exception {
		String message = UUID.randomUUID().toString();
		String response = get( "/callable?msg=" + message );
		assertEquals( "callable:" + message, response );

		Thread.sleep( 100 );

		verify( spyTaskExecutor ).submit( any( Runnable.class ) );
		verify( callableProcessingInterceptor ).afterCompletion( any(), any() );
		verify( deferredResultProcessingInterceptor, never() ).afterCompletion( any(), any() );
	}

	@Test
	public void deferredResultShouldWork() throws Exception {
		String message = UUID.randomUUID().toString();
		String response = get( "/deferredResult?msg=" + message );
		assertEquals( "deferred:" + message, response );

		// Add a delay so deferred result processing would be completed
		Thread.sleep( 100 );

		verify( spyTaskExecutor, never() ).submit( any( Runnable.class ) );
		verify( callableProcessingInterceptor, never() ).afterCompletion( any(), any() );
		verify( deferredResultProcessingInterceptor ).afterCompletion( any(), any() );
	}

	@Configuration
	@EnableAcrossContext(modules = AsyncModule.NAME)
	protected static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public AsyncTaskExecutor spyTaskExecutor() {
			return spy( new SimpleAsyncTaskExecutor() );
		}

		@Bean
		public CallableProcessingInterceptor callableProcessingInterceptor() {
			return mock( CallableProcessingInterceptor.class );
		}

		@Bean
		public DeferredResultProcessingInterceptor deferredResultProcessingInterceptor() {
			return mock( DeferredResultProcessingInterceptor.class );
		}

		@Override
		public void configureAsyncSupport( AsyncSupportConfigurer configurer ) {
			configurer.setDefaultTimeout( 1000 );
			configurer.setTaskExecutor( spyTaskExecutor() );
			configurer.registerCallableInterceptors( callableProcessingInterceptor() );
			configurer.registerDeferredResultInterceptors( deferredResultProcessingInterceptor() );
		}
	}
}
