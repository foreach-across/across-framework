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
package test.bootstrap;

import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.info.AcrossContextInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.bootstrap.one.CustomConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer} behaviour.
 * Also tests the following issues:
 * - AX-119: extending modules with classes with the same simple name should not override each other
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = TestSameNameBeanOverriding.Config.class)
public class TestSameNameBeanOverriding
{
	private ApplicationContext module;

	@Autowired
	private void attachModuleApplicationContext( AcrossContextInfo contextInfo ) {
		module = contextInfo.getModuleInfo( "myModule" ).getApplicationContext();
	}

	@Test
	public void sameNameComponentsAreDifferent() {
		assertEquals( 3, module.getBeansOfType( MyInterface.class ).size() );
	}

	@Test
	public void allConfigurationBeansShouldHaveBeenAdded() {
		Map<String, Long> values = module.getBeansOfType( Long.class );
		assertEquals( 3, values.size() );
		assertTrue( values.containsValue( 1L ) );
		assertTrue( values.containsValue( 2L ) );
		assertTrue( values.containsValue( 3L ) );
	}

	@Test
	public void beanDefinitionShouldBeOverruled() {
		assertEquals( 123, module.getBean( "myBean" ) );
	}

	@Configuration
	@EnableAcrossContext
	static class Config implements AcrossBootstrapConfigurer
	{
		@Bean
		public EmptyAcrossModule myModule() {
			return new EmptyAcrossModule( "myModule" );
		}

		@Override
		public void configureContext( AcrossBootstrapConfig contextConfiguration ) {
			contextConfiguration.extendModule( "myModule", CustomConfiguration.class, test.bootstrap.three.CustomComponent.class );
			contextConfiguration.extendModule( "myModule", test.bootstrap.one.CustomComponent.class.getName(),
			                                   test.bootstrap.three.CustomConfiguration.class.getName() );
		}

		@Override
		public void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
			if ( "myModule".equalsIgnoreCase( moduleConfiguration.getModuleName() ) ) {
				moduleConfiguration.addApplicationContextConfigurer( test.bootstrap.two.CustomConfiguration.class,
				                                                     test.bootstrap.two.CustomComponent.class );
			}
		}
	}
}
