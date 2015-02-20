package com.foreach.across.test.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test that checks that bootstrap fails if an exception occurs during event handling.
 */
public class TestBootstrap
{
	private static final Set<String> events = new HashSet<>();

	@Before
	public void clearEvents() {
		events.clear();
	}

	@Test
	public void bootstrapFailsOnEventErrorsByDefault() {
		AcrossContext ctx = new AcrossContext();

		EmptyAcrossModule one = new EmptyAcrossModule( "one" );
		one.addApplicationContextConfigurer( Config.class );

		EmptyAcrossModule two = new EmptyAcrossModule( "two" );
		two.addApplicationContextConfigurer( Config.class );

		EmptyAcrossModule three = new EmptyAcrossModule( "three" );
		three.addApplicationContextConfigurer( Config.class );

		ctx.addModule( one );
		ctx.addModule( two );
		ctx.addModule( three );

		boolean caught = false;

		try {
			ctx.bootstrap();
		}
		catch ( AcrossException ae ) {
			caught = true;
		}

		assertTrue( events.contains( "two" ) );
		assertTrue( events.contains( "three" ) );
		assertTrue( caught );
	}

	@Test
	public void bootstrapShouldNotFailOnEventHandlingIfSoConfigured() {
		AcrossContext ctx = new AcrossContext();
		ctx.setFailBootstrapOnEventPublicationErrors( false );

		EmptyAcrossModule one = new EmptyAcrossModule( "one" );
		one.addApplicationContextConfigurer( Config.class );

		EmptyAcrossModule two = new EmptyAcrossModule( "two" );
		two.addApplicationContextConfigurer( Config.class );

		EmptyAcrossModule three = new EmptyAcrossModule( "three" );
		three.addApplicationContextConfigurer( Config.class );

		ctx.addModule( one );
		ctx.addModule( two );
		ctx.addModule( three );

		ctx.bootstrap();

		assertTrue( events.contains( "two" ) );
		assertTrue( events.contains( "three" ) );
	}

	@Configuration
	@AcrossEventHandler
	protected static class Config
	{
		@Event
		protected void moduleBootstrapEvent( AcrossModuleBootstrappedEvent event ) {
			events.add( event.getModule().getName() );

			if ( event.getModule().getName().equals( "three" ) ) {
				throw new RuntimeException( "three has failed" );
			}
		}
	}
}
