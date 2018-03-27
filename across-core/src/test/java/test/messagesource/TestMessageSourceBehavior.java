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
package test.messagesource;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Internal;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.context.support.AcrossModuleMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests messagesource behavior in modules combined with ApplicationContext level messagesources.
 *
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMessageSourceBehavior.Config.class)
public class TestMessageSourceBehavior
{
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void messageFromAdditionalMessageSource() {
		assertEquals( "size must be between {min} and {max}",
		              message( "javax.validation.constraints.Size.message" ) );
	}

	@Test
	public void lowestModuleAppliesInModules() {
		assertEquals( "module1", moduleValue( "module1" ) );
		assertEquals( "module2", moduleValue( "module2" ) );
		assertEquals( "module3", moduleValue( "module3" ) );
		assertEquals( "module4", moduleValue( "module4" ) );

		assertEquals( "module3", moduleName( "module1" ) );
		assertEquals( "module3", moduleName( "module2" ) );
		assertEquals( "module3", moduleName( "module3" ) );
		assertEquals( "module4", moduleName( "module4" ) );
	}

	@Test
	public void lowestModuleValueAppliesOutsideContext() {
		assertEquals( "module3", message( "module.name" ) );
		assertEquals( "module1", message( "module1.value" ) );
		assertEquals( "module2", message( "module2.value" ) );
		assertEquals( "module3", message( "module3.value" ) );
	}

	@Test(expected = NoSuchMessageException.class)
	public void internalModuleMessageSourceIsNotVisibleOutside() {
		message( "module4.value" );
	}

	@Test
	public void contextValuesTrumpsModule() {
		assertEquals( "context", message( "context.name" ) );
		assertEquals( "context", contextName( "module1" ) );
		assertEquals( "context", contextName( "module2" ) );
		assertEquals( "context", contextName( "module3" ) );
		assertEquals( "module4", contextName( "module4" ) );
	}

	@Test
	public void bootstrapMessageValuesDependOnModulesLoaded() {
		assertEquals( "module1", moduleNameDuringBootstrap( "module1" ) );
		assertEquals( "module2", moduleNameDuringBootstrap( "module2" ) );
		assertEquals( "module3", moduleNameDuringBootstrap( "module3" ) );
		assertEquals( "module4", moduleNameDuringBootstrap( "module4" ) );
	}

	@Test
	public void contextValuesTrumpsModuleAlsoDuringBootstrap() {
		assertEquals( "context", contextNameDuringBootstrap( "module1" ) );
		assertEquals( "context", contextNameDuringBootstrap( "module2" ) );
		assertEquals( "context", contextNameDuringBootstrap( "module3" ) );
		assertEquals( "module4", contextNameDuringBootstrap( "module4" ) );
	}

	@Test
	public void defaultMessageSourcesShouldBeLoaded() {
		assertEquals( "module5", message( "module5.value" ) );
		assertEquals( "module5", message( "module5.default.value" ) );
		assertEquals( "module6", message( "module6.value" ) );
		assertEquals( "module6", message( "module6.message.value" ) );
		assertEquals( "module6", message( "module6.more.value" ) );
	}

	@Test
	public void fallbackLookupShouldNotUseSystemLocale() {
		Locale systemLocale = Locale.getDefault();
		try {
			Locale.setDefault( Locale.ENGLISH );

			assertEquals( "modulesix", messageSource.getMessage( "module6.more.value", new Object[0], Locale.FRENCH ) );
			assertEquals( "module6", messageSource.getMessage( "module6.more.value", new Object[0], Locale.ENGLISH ) );
			assertEquals( "module6base", messageSource.getMessage( "module6.more.value", new Object[0], Locale.CHINA ) );

		}
		finally {
			Locale.setDefault( systemLocale );
		}
	}

	private String contextName( String module ) {
		return beanRegistry.getBeanOfTypeFromModule( module, ModuleConfig.class ).getMessage( "context.name" );
	}

	private String contextNameDuringBootstrap( String module ) {
		return beanRegistry.getBeanOfTypeFromModule( module, ModuleConfig.class ).getMessageDuringBootstrap(
				"context.name" );
	}

	private String moduleValue( String module ) {
		return beanRegistry.getBeanOfTypeFromModule( module, ModuleConfig.class ).getMessage( module + ".value" );
	}

	private String moduleName( String module ) {
		return beanRegistry.getBeanOfTypeFromModule( module, ModuleConfig.class ).getMessage( "module.name" );
	}

	private String moduleNameDuringBootstrap( String module ) {
		return beanRegistry.getBeanOfTypeFromModule( module, ModuleConfig.class )
		                   .getMessageDuringBootstrap( "module.name" );
	}

	private String message( String key ) {
		return messageSource.getMessage( key, new Object[0], Locale.US );
	}

	@Configuration
	protected static class Config
	{
		@Autowired
		private ApplicationContext parent;

		@Bean
		public MessageSource messageSource() {
			ResourceBundleMessageSource source = new ResourceBundleMessageSource();
			source.setBasename( "test.messagesource.context" );

			return source;
		}

		@Bean
		public AcrossContext acrossContext() {
			AcrossContext ctx = new AcrossContext( parent );
			ctx.addModule( new ConfiguredMessageModule( "module1", false ) );
			ctx.addModule( new ConfiguredMessageModule( "module2", false ) );
			ctx.addModule( new ConfiguredMessageModule( "module3", false ) );
			ctx.addModule( new ConfiguredMessageModule( "module4", true ) );
			ctx.addModule( new DefaultMessageModule( "module5" ) );
			ctx.addModule( new DefaultMessageModule( "module6" ) );
			ctx.addModule( new DefaultMessageModule( "noMessageSourceModule7" ) );

			ctx.bootstrap();

			return ctx;
		}
	}

	protected static class ConfiguredMessageModule extends AcrossModule
	{
		private final String name;

		private ConfiguredMessageModule( String name, boolean internal ) {
			this.name = name;

			if ( internal ) {
				addApplicationContextConfigurer( InternalModuleConfig.class );
			}
			else {
				addApplicationContextConfigurer( ModuleConfig.class );
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return "Module with manually defined message source bean.";
		}

		@Override
		protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {

		}
	}

	protected static class DefaultMessageModule extends AcrossModule
	{
		private final String name;

		public DefaultMessageModule( String name ) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return "Module without any explicit message source configuration will scan for default messages.";
		}
	}

	@Configuration
	protected static class ModuleConfig
	{
		@Autowired
		protected AcrossModuleInfo module;

		public String getMessageDuringBootstrap( String code ) {
			return bootstrapMessages().get( code );
		}

		public String getMessage( String code ) {
			return messageSource().getMessage( code, new Object[0], Locale.ENGLISH );
		}

		@Bean
		public BootstrapMessages bootstrapMessages() {
			return new BootstrapMessages();
		}

		@Bean
		public MessageSource messageSource() {
			AcrossModuleMessageSource source = new AcrossModuleMessageSource();
			source.setBasenames(
					"classpath:/org/hibernate/validator/ValidationMessages",
					"classpath:/test/messagesource/" + module.getName()
			);

			return source;
		}

		public static class BootstrapMessages
		{
			@Autowired
			private MessageSource messageSource;

			private Map<String, String> bootstrapMessages = new HashMap<>();

			@PostConstruct
			public void registerBootstrapMessages() {
				bootstrapMessages.put( "module.name", getMessage( "module.name" ) );
				bootstrapMessages.put( "context.name", getMessage( "context.name" ) );
			}

			public String getMessage( String code ) {
				return messageSource.getMessage( code, new Object[0], Locale.ENGLISH );
			}

			public String get( String code ) {
				return bootstrapMessages.get( code );
			}
		}
	}

	@Configuration
	protected static class InternalModuleConfig extends ModuleConfig
	{
		@Bean
		@Internal
		@Override
		public MessageSource messageSource() {
			ResourceBundleMessageSource source = new ResourceBundleMessageSource();
			source.setBasename( "test.messagesource." + module.getName() );

			return source;
		}
	}
}
