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

package com.foreach.across.test.exposing;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.installers.InstallerAction;
import com.foreach.across.core.transformers.BeanPrefixingTransformer;
import com.foreach.across.test.AbstractInlineModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestCurrentModuleWiring.Config.class)
@DirtiesContext
public class TestCurrentModuleWiring
{
	@Autowired
	private AcrossContext acrossContext;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Autowired(required = false)
	@Module("ModuleOne")
	private ModuleConfig.BeanWithCurrentModules beanFromOne;

	@Autowired(required = false)
	@Module("ModuleTwo")
	private ModuleConfig.BeanWithCurrentModules beanFromTwo;

	@Test
	public void verifyCurrentModuleWiredCorrectly() {
		AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo( acrossContext );
		assertNotNull( contextInfo );

		AcrossModuleInfo moduleOne = contextInfo.getModuleInfo( "ModuleOne" );
		assertNotNull( moduleOne );
		ModuleConfig.BeanWithCurrentModules beanWithCurrentModules = beanRegistry.getBeanFromModule( "ModuleOne",
		                                                                                             "beanWithCurrentModules" );
		assertNull( beanWithCurrentModules.getParent() );
		beanWithCurrentModules.assertCurrentModule( moduleOne.getModule() );

		AcrossModuleInfo moduleTwo = contextInfo.getModuleInfo( "ModuleTwo" );
		assertNotNull( moduleTwo );
		beanWithCurrentModules = beanRegistry.getBeanFromModule( "ModuleTwo", "beanWithCurrentModules" );
		beanWithCurrentModules.assertCurrentModule( moduleTwo.getModule() );

		assertNotNull( beanWithCurrentModules.getParent() );

		ModuleConfig.BeanWithCurrentModules parent = beanWithCurrentModules.getParent();
		parent.assertCurrentModule( moduleOne.getModule() );
	}

	@Test
	public void verifyBeansExposedToParentContext() {
		assertSame( beanFromOne, beanRegistry.getBeanFromModule( "ModuleOne", "beanWithCurrentModules" ) );
		assertSame( beanFromTwo, beanRegistry.getBeanFromModule( "ModuleTwo", "beanWithCurrentModules" ) );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public AcrossContext acrossContext( ApplicationContext applicationContext ) {
			AcrossContext context = new AcrossContext( applicationContext );
			context.setInstallerAction( InstallerAction.DISABLED );

			context.addModule( new ModuleOne() );
			context.addModule( new ModuleTwo() );

			return context;
		}
	}

	protected static class ModuleOne extends AbstractInlineModule
	{
		public ModuleOne() {
			super( "ModuleOne", ModuleConfig.class );

			setExposeTransformer( new BeanPrefixingTransformer( "parent" ) );
		}
	}

	protected static class ModuleTwo extends AbstractInlineModule
	{
		public ModuleTwo() {
			super( "ModuleTwo", ModuleConfig.class );
		}
	}

	@Configuration
	protected static class ModuleConfig
	{
		@Bean
		@Exposed
		public BeanWithCurrentModules beanWithCurrentModules() {
			return new BeanWithCurrentModules();
		}

		static class BeanWithCurrentModules
		{
			@Autowired(required = false)
			private BeanWithCurrentModules parent;

			@Autowired
			private AcrossModule currentModuleWithoutQualifier;

			@Autowired
			@Qualifier(AcrossModule.CURRENT_MODULE)
			private AcrossModule currentModuleByGeneralQualifier;

			@Autowired
			@Module(AcrossModule.CURRENT_MODULE)
			private AcrossModule currentModuleByModuleQualifier;

			public BeanWithCurrentModules getParent() {
				return parent;
			}

			public void assertCurrentModule( AcrossModule expected ) {
				assertSame( expected, currentModuleWithoutQualifier );
				assertSame( expected, currentModuleByGeneralQualifier );
				assertSame( expected, currentModuleByModuleQualifier );
			}
		}
	}

}
