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
package test.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossException;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * DevelopmentModeCondition that checks that bootstrap fails if an exception occurs during event handling.
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
			assertFalse( ae.getCause() instanceof IndexOutOfBoundsException );
		}

		assertTrue( events.contains( "two" ) );
		assertTrue( events.contains( "three" ) );
		assertTrue( caught );
	}

	@Configuration
	protected static class Config
	{
		@EventListener
		protected void moduleBootstrapEvent( AcrossModuleBootstrappedEvent event ) {
			events.add( event.getModule().getName() );

			if ( event.getModule().getName().equals( "three" ) ) {
				throw new RuntimeException( "three has failed" );
			}
		}
	}
}
