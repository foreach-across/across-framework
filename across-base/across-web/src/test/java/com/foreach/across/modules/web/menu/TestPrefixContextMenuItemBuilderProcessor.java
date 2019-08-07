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

package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestPrefixContextMenuItemBuilderProcessor
{
	private MenuItemBuilderProcessor processor;

	@BeforeEach
	public void createProcessor() {
		processor = new PrefixContextMenuItemBuilderProcessor( new PrefixingPathContext( "/test" ) );
	}

	@Test
	public void processMenuWithoutUrl() {
		Menu menu = new Menu( "path" );

		assertFalse( menu.hasUrl() );

		Menu processed = processor.process( menu );

		assertSame( menu, processed );
		assertEquals( "path", menu.getPath() );
		assertTrue( menu.hasUrl() );
		assertEquals( "/test/path", menu.getUrl() );

		Collection<String> matchers = menu.getAttribute(
				RequestMenuSelector.ATTRIBUTE_MATCHERS );

		assertNotNull( matchers );
		assertEquals( 1, matchers.size() );
		assertTrue( matchers.contains( "/test/path" ) );
	}

	@Test
	public void processGroupShouldNotTouchUrl() {
		Menu menu = new Menu( "path" );
		menu.setGroup( true );

		assertFalse( menu.hasUrl() );

		Menu processed = processor.process( menu );

		assertSame( menu, processed );
		assertEquals( "path", menu.getPath() );
		assertFalse( menu.hasUrl() );

		Collection<String> matchers = menu.getAttribute(
				RequestMenuSelector.ATTRIBUTE_MATCHERS );

		assertNotNull( matchers );
		assertEquals( 1, matchers.size() );
		assertTrue( matchers.contains( "/test/path" ) );
	}

	@Test
	public void processMenuWithUrl() {
		Menu menu = new Menu( "/second" );
		menu.setUrl( "/my/url" );
		menu.setAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS, Arrays.asList( "one", "two" ) );

		Menu processed = processor.process( menu );

		assertSame( menu, processed );
		assertEquals( "/second", menu.getPath() );
		assertEquals( "/test/my/url", menu.getUrl() );

		Collection<String> matchers = menu.getAttribute(
				RequestMenuSelector.ATTRIBUTE_MATCHERS );

		assertNotNull( matchers );
		assertEquals( 3, matchers.size() );
		assertTrue( matchers.contains( "one" ) );
		assertTrue( matchers.contains( "two" ) );
		assertTrue( matchers.contains( "/test/second" ) );
	}
}
