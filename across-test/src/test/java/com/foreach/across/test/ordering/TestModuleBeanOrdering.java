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
package com.foreach.across.test.ordering;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.ordering.one.ModuleOne;
import com.foreach.across.test.ordering.two.ModuleTwo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests that exposed beans show up in every module context,
 * and that ordering overall is respected.
 * <p>
 * module one:
 * - component one: OiM = 1, not exposed
 * - component two: OiM = 2, exposed
 * <p>
 * module two - depends on 1:
 * - component one: OiM = 2, exposed
 * - component two: OiM = 1, not exposed
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestModuleBeanOrdering.Config.class)
public class TestModuleBeanOrdering
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Autowired
	private AcrossContextInfo contextInfo;

	private ApplicationContext parentContext;
	private ApplicationContext moduleOne, moduleTwo;

	@Before
	public void setUp() throws Exception {
		moduleOne = contextInfo.getModuleInfo( "ModuleOne" ).getApplicationContext();
		moduleTwo = contextInfo.getModuleInfo( "ModuleTwo" ).getApplicationContext();
	}

	@Test
	public void moduleOneIncludesAllExposedBeans() {
		assertBeans(
				moduleOne.getBeansOfType( MyComponent.class ),
				// first the module internal beans should be returned in module order
				"moduleOneComponentOne", "moduleOneComponentTwo",
				// next the exposed bean from module two
				"moduleTwoComponentOne"
		);
		//assertBeans( moduleOne.getBeansOfType( MyComponent.class ), "moduleOneComponentOne", "moduleOneComponentTwo" );
	}

	@Test
	public void moduleTwoIncludesAllExposedBeansAsWell() {
		assertBeans(
				moduleTwo.getBeansOfType( MyComponent.class ),
				// first module one exposed bean
				"moduleOneComponentTwo",
				// next module 2 beans in module order
				"moduleTwoComponentTwo", "moduleTwoComponentOne"
		);
	}

	@Test
	public void contextWithoutInternalsReturnsAllExposedBeansInOrder() {
		assertBeans(
				beanRegistry.getBeansOfTypeAsMap( MyComponent.class, false ),
				// first module one exposed beans
				"moduleOneComponentTwo",
				// second module two exposed beans
				"moduleTwoComponentOne"
		);
	}

	@Test
	public void contextWithInternalsReturnsAlsoNonExposedBeansInOrder() {
		assertBeans(
				beanRegistry.getBeansOfTypeAsMap( MyComponent.class, true ),
				// first module beans in module order
				"ModuleOne:moduleOneComponentOne", "ModuleOne:moduleOneComponentTwo",
				// second module two beans in module order
				"ModuleTwo:moduleTwoComponentTwo", "ModuleTwo:moduleTwoComponentOne"
		);
	}

	@AcrossTestConfiguration
	static class Config
	{
		@Bean
		ModuleOne moduleOne() {
			return new ModuleOne();
		}

		@Bean
		ModuleTwo moduleTwo() {
			return new ModuleTwo();
		}
	}

	private void assertBeans( Map<String, MyComponent> beans, String... expected ) {
		assertEquals( expected.length, beans.size() );
		String[] beanNames = beans.keySet().toArray( new String[beans.size()] );
		assertArrayEquals( "Bean names: " + Arrays.toString( beanNames ), expected, beanNames );
	}
}
