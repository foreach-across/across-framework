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
package com.foreach.across.test.application;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.test.application.app.OverrideApplication;
import com.foreach.across.test.application.app.application.extensions.MyInterfaceFromApp;
import com.foreach.across.test.application.app.modules.MyInterface;
import com.foreach.across.test.application.app.modules.two.ComponentFromTwo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the bean definition overriding semantics in Across 3,
 * limited support for overriding bean definitions.
 *
 * @author Arne Vandamme
 * @since 3.2.1
 */
@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(classes = OverrideApplication.class)
public class TestApplicationBeanDefinitionOverriding
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void myInterfaceComponentInModuleOneShouldBeFromAppExtension() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		MyInterface myInterfaceComponent = moduleOne.getApplicationContext().getBean( MyInterface.class );
		assertThat( myInterfaceComponent ).isNotNull().isNotInstanceOf( MyInterfaceFromApp.class );
	}

	@Test
	public void componentFromOneInModuleOneShouldBeFromModuleTwo() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		Object componentFromOne = moduleOne.getApplicationContext().getBean( "componentFromOne" );
		assertThat( componentFromOne ).isNotNull().isInstanceOf( ComponentFromTwo.class );
	}

	@Test
	public void otherComponentFromOneInModuleOneShouldBeFromModuleTwo() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		Object componentFromOne = moduleOne.getApplicationContext().getBean( "otherComponentFromOne" );
		assertThat( componentFromOne ).isNotNull().isNotInstanceOf( ComponentFromTwo.class );
	}
}
