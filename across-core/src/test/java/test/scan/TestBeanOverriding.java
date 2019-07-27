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
package test.scan;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.scan.overriding.MyComponent;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the behaviour of Spring 4.2 with bean definition overriding.
 * See also https://jira.spring.io/browse/SPR-9567.
 *
 * @author Arne Vandamme
 * @since 1.1.3
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration
public class TestBeanOverriding
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void myComponentShouldBeTheReplacedVersion() {
		Object myComponent = beanRegistry.getBeanFromModule( "MyModule", "myComponent" );
		assertNotNull( myComponent );
		assertTrue( myComponent instanceof String );
		assertEquals( "myComponent", Objects.toString( myComponent ) );
	}

	@Test
	public void otherComponentShouldBeTheReplacedVersion() {
		Object otherComponent = beanRegistry.getBeanFromModule( "MyModule", "otherComponent" );
		assertNotNull( otherComponent );
		assertTrue( otherComponent instanceof String );
		assertEquals( "otherComponent", Objects.toString( otherComponent ) );
	}

	@Configuration
	@EnableAcrossContext
	protected static class Config
	{
		@Bean
		public MyModule myModule() {
			MyModule myModule = new MyModule();
			myModule.addApplicationContextConfigurer( BeanOverrides.class );
			return myModule;
		}
	}

	protected static class MyModule extends AcrossModule
	{
		@Override
		public String getName() {
			return "MyModule";
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
			contextConfigurers.add( new ComponentScanConfigurer( MyComponent.class ) );
			contextConfigurers.add( new AnnotatedClassConfigurer( MyModuleBeansConfiguration.class ) );
		}
	}

	protected static class MyModuleBeansConfiguration
	{
		@Bean
		public Date otherComponent() {
			return new Date();
		}
	}

	protected static class BeanOverrides
	{
		@Bean
		public String myComponent() {
			return "myComponent";
		}

		@Bean
		public String otherComponent() {
			return "otherComponent";
		}

	}
}
