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
import com.foreach.across.test.modules.naming.NamingConfig;
import com.foreach.across.test.modules.naming.NamingModule;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class TestDefaultProvidedBeans
{
	@Test
	public void rightProvidedBeansAreWiredInEachModule() {
		NamingModule firstModule = new NamingModule( "FirstModule" );
		NamingModule lastModule = new NamingModule( "LastModule" );

		AcrossContext context = new AcrossContext();
		context.addModule( firstModule );
		context.addModule( lastModule );
		context.bootstrap();

		NamingConfig first = AcrossContextUtils.getApplicationContext( firstModule ).getBean( NamingConfig.class );
		assertSame( context, first.getAutoAcrossContext() );
		assertSame( context, first.getSpecificAcrossContext() );
		assertSame( firstModule, first.getCurrentModule() );
		assertSame( firstModule, first.getAutoNamingModule() );
		assertSame( firstModule, first.getModuleNamedFirst() );
		assertSame( lastModule, first.getModuleNamedLast() );

		NamingConfig last = AcrossContextUtils.getApplicationContext( lastModule ).getBean( NamingConfig.class );
		assertSame( context, last.getAutoAcrossContext() );
		assertSame( context, last.getSpecificAcrossContext() );
		assertSame( lastModule, last.getCurrentModule() );
		assertSame( lastModule, last.getAutoNamingModule() );
		assertSame( firstModule, last.getModuleNamedFirst() );
		assertSame( lastModule, last.getModuleNamedLast() );
	}
}
