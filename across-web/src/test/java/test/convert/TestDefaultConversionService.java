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
package test.convert;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.web.StandardAcrossServletEnvironment;
import com.foreach.across.modules.web.AcrossWebModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * AcrossWebModule creates a ConversionService called mvcConversionService.
 * By default this ConversionService is the same as the parent.
 *
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestDefaultConversionService.Config.class)
public class TestDefaultConversionService
{
	@Autowired(required = false)
	@Qualifier(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME)
	private ConversionService conversionService;

	@Autowired(required = false)
	@Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN)
	private ConversionService mvcConversionService;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void mvcConversionServiceExistsAndIsExposed() {
		assertNotNull( conversionService );
		assertNotNull( mvcConversionService );
		assertSame( conversionService, mvcConversionService );

		ApplicationContext ctx = contextInfo.getModuleInfo( AcrossWebModule.NAME ).getApplicationContext();

		ConversionService mvcConversionService = ctx.getBean( "mvcConversionService", ConversionService.class );
		assertSame( conversionService, mvcConversionService );
	}

	@Test
	public void defaultConversionServiceShouldBeAttachedToContextAndAcrossWebModule() {
		ConfigurableEnvironment contextEnvironment =
				(ConfigurableEnvironment) contextInfo.getApplicationContext().getEnvironment();
		assertSame( conversionService, contextEnvironment.getConversionService() );

		StandardAcrossServletEnvironment moduleEnvironment = (StandardAcrossServletEnvironment) contextInfo
				.getModuleInfo( AcrossWebModule.NAME )
				.getApplicationContext()
				.getEnvironment();

		assertSame( conversionService, moduleEnvironment.getConversionService() );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}
	}
}
