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
package com.foreach.across.test.application;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.test.application.app.OverrideApplication;
import com.foreach.across.test.application.app.application.extensions.MyInterfaceFromApp;
import com.foreach.across.test.application.app.modules.MyInterface;
import com.foreach.across.test.application.app.modules.two.ComponentFromTwo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.2.1
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@SpringBootTest(classes = OverrideApplication.class)
class TestApplicationBeanDefinitionOverriding
{
	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	void myInterfaceComponentInModuleOneShouldBeFromAppExtension() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		MyInterface myInterfaceComponent = moduleOne.getApplicationContext().getBean( MyInterface.class );
		assertThat( myInterfaceComponent ).isNotNull().isInstanceOf( MyInterfaceFromApp.class );
	}

	@Test
	void componentFromOneInModuleOneShouldBeFromModuleTwo() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		Object componentFromOne = moduleOne.getApplicationContext().getBean( "componentFromOne" );
		assertThat( componentFromOne ).isNotNull().isInstanceOf( ComponentFromTwo.class );
	}

	@Test
	void otherComponentFromOneInModuleOneShouldBeFromModuleTwo() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		Object componentFromOne = moduleOne.getApplicationContext().getBean( "otherComponentFromOne" );
		assertThat( componentFromOne ).isNotNull().isInstanceOf( ComponentFromTwo.class );
	}

	@Test
	@DisplayName( "Non-deferred @ModuleConfiguration should come before original module" )
	void conditionalComponentInOneShouldNotBeCreated() {
		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		assertThat( moduleOne.getApplicationContext().getBean( "nonDeferredBeanFromApplication" ) ).isNotNull();
		assertThat( moduleOne.getApplicationContext().containsBeanDefinition( "conditionalComponent" ) ).isFalse();
	}
}
