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
package test.lifecycle;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that exposed factory beans keep type information as detailed as possible.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
@SuppressWarnings("all")
public class TestAcrossLifecycleProcessor
{
	private AcrossContext context;
	private GenericApplicationContext parent;

	@Before
	public void startAcrossContext() {
		parent = new GenericApplicationContext();
		parent.refresh();

		context = new AcrossContext( parent );
		context.addModule( new EmptyAcrossModule( "FirstModule", BeanConfiguration.class ) );
		context.addModule( new EmptyAcrossModule( "SecondModule" ) );
		context.bootstrap();

		parent.start();
	}

	@After
	public void destroyAcrossContext() {
		context.shutdown();
		parent.close();
	}

	@Test
	public void lifecycleBeanShouldOnlyBeStartedOnce() {
		AcrossContextBeanRegistry beanRegistry = AcrossContextUtils.getBeanRegistry( context );

		MyBean first = beanRegistry.getBeanOfTypeFromModule( "FirstModule", MyBean.class );
		MyBean second = beanRegistry.getBeanOfTypeFromModule( "SecondModule", MyBean.class );
		MyBean third = beanRegistry.getBeanOfType( MyBean.class );

		assertThat( first ).isSameAs( second ).isSameAs( third );

		assertThat( first.getStarted() ).isEqualTo( 1 );
	}

	@Configuration
	static class BeanConfiguration
	{
		@Bean
		@Exposed
		public MyBean myBean() {
			return new MyBean();
		}
	}

	static class MyBean implements Lifecycle
	{
		@Getter
		private int started;

		@Override
		public void start() {
			started++;
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean isRunning() {
			return false;
		}
	}
}
