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
package test.exposing;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import test.modules.exposing.MyPrototypeBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 1.1.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestDefaultExposeFilter.Config.class)
@DirtiesContext
public class TestAcrossContextBeanRegistry
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void exposedPrototypeBeanShouldBeFoundOnceInRoot() {
		List<MyPrototypeBean> beans = beanRegistry.getBeansOfType( MyPrototypeBean.class );
		assertEquals( 1, beans.size() );
		assertNotNull( beans.get( 0 ) );
	}

	@Test
	public void exposedPrototypeBeanShouldBeFoundOnceWithInternalModulesLookup() {
		List<MyPrototypeBean> beans = beanRegistry.getBeansOfType( MyPrototypeBean.class, true );
		assertEquals( 1, beans.size() );
		assertNotNull( beans.get( 0 ) );
	}
}
