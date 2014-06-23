package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItemBuilderProcessor;
import com.foreach.across.modules.web.menu.PrefixContextMenuItemBuilderProcessor;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

public class TestPrefixContextMenuItemBuilderProcessor
{
	private MenuItemBuilderProcessor processor;

	@Before
	public void createProcessor() {
		processor = new PrefixContextMenuItemBuilderProcessor( new PrefixingPathContext( "/test" ) );
	}

	@Test
	public void processMenuWithoutUrl() {
		Menu menu = new Menu( "path" );

		Menu processed = processor.process( menu );

		assertSame( menu, processed );
		assertEquals( "path", menu.getPath() );
		assertEquals( "/test/path", menu.getUrl() );

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
