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

package test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.menu.RequestMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestRequestScopedBeans.Config.class)
public class TestRequestScopedBeans
{
	@Autowired(required = false)
	private RequestMenuBuilder requestMenuBuilder;

	@Autowired(required = false)
	private RequestMenuStore requestMenuStore;

	@Autowired(required = false)
	private Component component;

	@Test
	public void scopedTargetsCreated() {
		assertNotNull( requestMenuBuilder );
		assertNotNull( requestMenuStore );
	}

	@Test
	public void scopedTargetCanBeUsedInOtherModule() {
		assertNotNull( component.getRequestMenuBuilder() );
		assertSame( requestMenuBuilder, component.getRequestMenuBuilder() );
	}

	@EnableAcrossContext
	@Configuration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
			context.addModule( testModule() );
		}

		private AcrossModule testModule() {
			AcrossModule module = new EmptyAcrossModule( "TestModule" );
			module.addApplicationContextConfigurer( ComponentConfig.class );

			return module;
		}
	}

	@Configuration
	static class ComponentConfig
	{
		@Bean
		@Exposed
		public Component component() {
			return new Component();
		}
	}

	static class Component
	{
		@Autowired
		private RequestMenuBuilder requestMenuBuilder;

		public RequestMenuBuilder getRequestMenuBuilder() {
			return requestMenuBuilder;
		}
	}
}
