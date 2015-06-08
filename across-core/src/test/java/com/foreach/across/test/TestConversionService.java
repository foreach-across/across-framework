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
package com.foreach.across.test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.StandardAcrossEnvironment;
import com.foreach.across.test.modules.exposing.ExposingModule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.ConfigurableEnvironment;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestConversionService
{
	private GenericApplicationContext parent;
	private AcrossContext context;

	@Before
	public void createContexts() {
		parent = new GenericApplicationContext();
		parent.refresh();

		context = new AcrossContext( parent );
	}

	@After
	public void destroyContexts() {
		context.shutdown();
		parent.close();
	}

	@Test
	public void createdByDefault() {
		context.bootstrap();

		ConversionService conversionService = parent.getBean(
				ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class );

		assertNotNull( conversionService );
	}

	@Test
	public void defaultDateConversions() throws ParseException {
		context.bootstrap();

		ConversionService conversionService = parent.getBean( ConversionService.class );

		Date date = DateUtils.parseDate( "2015-05-01", "yyyy-MM-dd" );
		Date dateWithTime = DateUtils.parseDate( "2015-05-01 13:38", "yyyy-MM-dd HH:mm" );
		Date dateWithTimestamp = DateUtils.parseDate( "2015-05-01 13:38:30", "yyyy-MM-dd HH:mm:ss" );

		assertNull( conversionService.convert( "", Date.class ) );
		assertNull( conversionService.convert( " ", Date.class ) );

		assertEquals( date, conversionService.convert( "2015-05-01", Date.class ) );
		assertEquals( dateWithTime, conversionService.convert( "2015-05-01 13:38", Date.class ) );
		assertEquals( dateWithTimestamp, conversionService.convert( "2015-05-01 13:38:30", Date.class ) );

		assertEquals( date, conversionService.convert( "2015-May-01", Date.class ) );
		assertEquals( dateWithTime, conversionService.convert( "2015-May-01 13:38", Date.class ) );
		assertEquals( dateWithTimestamp, conversionService.convert( "2015-May-01 13:38:30", Date.class ) );

		assertEquals( date, conversionService.convert( "1 May 2015", Date.class ) );
		assertEquals( date, conversionService.convert( "May 1, 2015", Date.class ) );
		assertEquals( date, conversionService.convert( "Friday, May 1, 2015", Date.class ) );
		assertEquals( date, conversionService.convert( "Fri, May 1, 2015", Date.class ) );

		assertEquals( dateWithTime, conversionService.convert( "1 May 2015 13:38", Date.class ) );
		assertEquals( dateWithTime, conversionService.convert( "May 1, 2015 13:38", Date.class ) );
		assertEquals( dateWithTime, conversionService.convert( "Friday, May 1, 2015 13:38", Date.class ) );
		assertEquals( dateWithTime, conversionService.convert( "Fri, May 1, 2015 13:38", Date.class ) );

		assertEquals( dateWithTimestamp, conversionService.convert( "1 May 2015 13:38:30", Date.class ) );
		assertEquals( dateWithTimestamp, conversionService.convert( "May 1, 2015 13:38:30", Date.class ) );
		assertEquals( dateWithTimestamp, conversionService.convert( "Friday, May 1, 2015 13:38:30", Date.class ) );
		assertEquals( dateWithTimestamp, conversionService.convert( "Fri, May 1, 2015 13:38:30", Date.class ) );

		assertEquals( dateWithTime, conversionService.convert( dateWithTime.toString(), Date.class ) );
	}

	@Test
	public void conversionServiceShouldBeRegisteredOnBothContextAndModuleEnvironments() {
		context.addModule( new ExposingModule( "test" ) );
		context.bootstrap();

		ConversionService conversionService = parent.getBean( ConversionService.class );
		assertNotNull( conversionService );

		ConfigurableEnvironment contextEnvironment = AcrossContextUtils.getApplicationContext( context ).getEnvironment();
		assertSame( conversionService, contextEnvironment.getConversionService() );

		StandardAcrossEnvironment moduleEnvironment = (StandardAcrossEnvironment) AcrossContextUtils
				.getContextInfo( context )
				.getModuleInfo( "test" )
				.getApplicationContext()
				.getEnvironment();

		assertSame( conversionService, moduleEnvironment.getConversionService() );
	}

	@Test
	public void createdAndPrimaryIfOtherConversionServiceHasWrongName() {
		ConversionService other = new GenericConversionService();
		parent.getBeanFactory().registerSingleton( "otherConversionService", other );

		context.bootstrap();

		ConversionService conversionService = parent.getBean(
				ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class );

		assertNotNull( conversionService );
		assertNotSame( other, conversionService );
		assertEquals( 2, BeanFactoryUtils.beansOfTypeIncludingAncestors( parent, ConversionService.class ).size() );

		ConversionService primary = AcrossContextUtils.getApplicationContext( context )
		                                              .getBean( ConversionService.class );
		assertSame( conversionService, primary );
	}

	@Test
	public void createdAndPrimaryIfOtherConversionServiceHasWrongType() {
		Map other = new HashMap<>();
		parent.getBeanFactory().registerSingleton( "otherConversionService", other );

		context.bootstrap();

		ConversionService conversionService = parent.getBean(
				ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class );

		assertNotNull( conversionService );
		assertNotSame( other, conversionService );
		assertEquals( 1, BeanFactoryUtils.beansOfTypeIncludingAncestors( parent, ConversionService.class ).size() );

		ConversionService primary = AcrossContextUtils.getApplicationContext( context )
		                                              .getBean( ConversionService.class );
		assertSame( conversionService, primary );
	}

	@Test
	public void notCreatedIfOtherConversionServiceWithRightNamePresent() {
		ConversionService other = new GenericConversionService();
		parent.getBeanFactory().registerSingleton( ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, other );

		context.bootstrap();

		ConversionService conversionService = parent.getBean(
				ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class );

		assertNotNull( conversionService );
		assertSame( other, conversionService );
		assertEquals( 1, BeanFactoryUtils.beansOfTypeIncludingAncestors( parent, ConversionService.class ).size() );

		ConversionService primary = AcrossContextUtils.getApplicationContext( context )
		                                              .getBean( ConversionService.class );
		assertSame( conversionService, primary );
	}
}
