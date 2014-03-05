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
