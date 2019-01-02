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
package test.convert;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * If ConversionService named mvcConversionService already exists, that one will be used instead.
 * The context ConversionService will be unmodified.
 *
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestExistingMvcConversionService.Config.class)
public class TestExistingMvcConversionService
{
	private static final Formatter<String> FORMATTER = new Formatter<String>()
	{
		@Override
		public String parse( String text, Locale locale ) throws ParseException {
			return null;
		}

		@Override
		public String print( String object, Locale locale ) {
			return null;
		}
	};

	private static final FormattingConversionService CONVERSION_SERVICE = mock( FormattingConversionService.class );

	@Autowired
	@Qualifier(ConfigurableWebApplicationContext.CONVERSION_SERVICE_BEAN_NAME)
	private ConversionService conversionService;

	@Autowired
	@Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN)
	private FormattingConversionService mvcConversionService;

	@Autowired
	private AcrossContextInfo contextInfo;

	@Test
	public void mvcConversionServiceWasNotCreatedInWebModule() {
		assertNotNull( conversionService );
		assertNotNull( mvcConversionService );
		assertNotSame( conversionService, mvcConversionService );
		assertSame( CONVERSION_SERVICE, mvcConversionService );

		ApplicationContext ctx = contextInfo.getModuleInfo( AcrossWebModule.NAME ).getApplicationContext();

		ConversionService mvcConversionServiceFromModule = ctx.getBean( AcrossWebModule.CONVERSION_SERVICE_BEAN,
		                                                                ConversionService.class );
		assertSame( mvcConversionService, mvcConversionServiceFromModule );

		verify( mvcConversionService ).addFormatter( FORMATTER );

	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements WebMvcConfigurer, AcrossContextConfigurer
	{
		@Bean
		public FormattingConversionService mvcConversionService() {
			reset( CONVERSION_SERVICE );
			return CONVERSION_SERVICE;
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}

		@Override
		public void addFormatters( FormatterRegistry registry ) {
			registry.addFormatter( FORMATTER );
		}
	}
}
