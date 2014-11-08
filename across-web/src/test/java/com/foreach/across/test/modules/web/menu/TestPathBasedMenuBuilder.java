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

package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuItemBuilderProcessor;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class TestPathBasedMenuBuilder
{
	private PathBasedMenuBuilder builder;

	@Before
	public void createBuilder() {
		builder = new PathBasedMenuBuilder();
	}

	@Test
	public void itemBuilder() {
		builder.item( "child", "Child", "url" );

		Menu menu = builder.build();

		assertEquals( 1, menu.size() );
		verify( menu.getFirstItem(), "child", "Child", "url" );

		menu = builder.item( "child" ).title( "test" ).and().build();
		assertEquals( 1, menu.size() );
		verify( menu.getFirstItem(), "child", "test", "url" );
	}

	@Test
	public void requestMatcherAttributes() {
		builder.item( "item" ).matchRequests( "/test/", "/other" );

		Menu menu = builder.build();

		Collection<String> matchers = menu.getFirstItem().getAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS );
		assertEquals( 2, matchers.size() );
		assertTrue( matchers.contains( "/test/" ) );
		assertTrue( matchers.contains( "/other" ) );
	}

	@Test
	public void buildItems() {
		builder.item( "1" ).title( "Title one" ).url( "URL one" );
		builder.item( "2" ).title( "Title two" ).url( "URL two" ).disable();
		builder.item( "3" ).title( "Title three" ).url( "URL three" ).order( 33 ).options( "option-one", "option-two" );
		builder.item( "4" ).title( "Title four" ).group( true ).attribute( "attribute-one", "value" ).attribute(
				"attribute-two", "value2" ).options( "test-option" );

		Menu menu = builder.build();
		assertEquals( 4, menu.size() );

		Menu one = menu.getItems().get( 0 );
		assertEquals( "1", one.getPath() );
		assertEquals( "Title one", one.getTitle() );
		assertEquals( "URL one", one.getUrl() );
		assertFalse( one.isDisabled() );
		assertFalse( one.isGroup() );
		assertTrue( one.getAttributes().isEmpty() );

		Menu two = menu.getItems().get( 1 );
		assertEquals( "2", two.getPath() );
		assertEquals( "Title two", two.getTitle() );
		assertEquals( "URL two", two.getUrl() );
		assertTrue( two.isDisabled() );
		assertFalse( two.isGroup() );
		assertTrue( two.getAttributes().isEmpty() );

		Menu three = menu.getItems().get( 2 );
		assertEquals( "3", three.getPath() );
		assertEquals( "Title three", three.getTitle() );
		assertEquals( "URL three", three.getUrl() );
		assertFalse( three.isDisabled() );
		assertFalse( three.isGroup() );
		assertEquals( 33, three.getOrder() );
		assertEquals( 2, three.getAttributes().size() );
		assertTrue( three.hasAttribute( "option-one" ) );
		assertTrue( three.hasAttribute( "option-two" ) );

		Menu four = menu.getItems().get( 3 );
		assertEquals( "4", four.getPath() );
		assertEquals( "Title four", four.getTitle() );
		assertFalse( four.hasUrl() );
		assertFalse( four.isDisabled() );
		assertTrue( four.isGroup() );
		assertEquals( 3, four.getAttributes().size() );
		assertTrue( four.hasAttribute( "test-option" ) );
		assertEquals( "value", four.getAttribute( "attribute-one" ) );
		assertEquals( "value2", four.getAttribute( "attribute-two" ) );
	}

	@Test
	public void concatenatingItems() {
		builder.root( "home" ).title( "Home" ).url( "/home" )
		       .and()
		       .group( "/news", "News" ).url( "http://news-section" )
		       .and()
		       .item( "/news/international/australia", "Australia", "aussies" )
		       .and()
		       .item( "/news/international", "International news", "intnl" )
		       .and()
		       .item( "/news/national", "National news", "national" )
		       .and()
		       .item( "/news/nationalAlmost", "Almost national news", "nationalAlmost" );

		Menu cursor = builder.build();
		assertNotNull( cursor );

		cursor = verify( cursor, "home", "Home", "/home" );
		cursor = verify( cursor.getFirstItem(), "/news", "News", "http://news-section" );

		Menu news = cursor;
		assertTrue( news.isGroup() );
		cursor = verify( news.getItems().get( 0 ), "/news/international", "International news", "intnl" );
		verify( cursor.getFirstItem(), "/news/international/australia", "Australia", "aussies" );

		verify( news.getItems().get( 1 ), "/news/national", "National news", "national" );
		verify( news.getItems().get( 2 ), "/news/nationalAlmost", "Almost national news", "nationalAlmost" );
	}

	@Test
	public void movingGeneratesADifferentMenuButKeepsPathIntact() {
		builder.group( "/users", "User management" ).and()
		       .item( "/users/roles", "User roles" ).and()
		       .item( "/users/users", "Users" ).and()
		       .item( "/loggers", "Loggers" ).and()
		       .group( "/administration", "Administration" ).and()
		       .item( "/administration/system-info", "System info" );

		Menu menu = builder.build();
		assertEquals( 3, menu.size() );

		Menu administration = menu.getItems().get( 0 );
		verify( administration, "/administration", "Administration", "/administration" );
		verify( administration.getFirstItem(), "/administration/system-info", "System info",
		        "/administration/system-info" );

		verify( menu.getItems().get( 1 ), "/loggers", "Loggers", "/loggers" );

		Menu userManagement = menu.getItems().get( 2 );
		verify( userManagement, "/users", "User management", "/users" );
		verify( userManagement.getItems().get( 0 ), "/users/roles", "User roles", "/users/roles" );
		verify( userManagement.getItems().get( 1 ), "/users/users", "Users", "/users/users" );

		builder.move( "/users", "/administration/users" )
		       .move( "/loggers", "/administration/loggers" );

		// Items should have been moved, but still the same data
		menu = builder.build();
		assertEquals( 1, menu.size() );

		administration = menu.getItems().get( 0 );
		verify( administration, "/administration", "Administration", "/administration" );
		assertEquals( 3, administration.size() );

		verify( administration.getItems().get( 0 ), "/loggers", "Loggers", "/loggers" );

		verify( administration.getItems().get( 1 ), "/administration/system-info", "System info",
		        "/administration/system-info" );

		userManagement = administration.getItems().get( 2 );
		verify( userManagement, "/users", "User management", "/users" );
		verify( userManagement.getItems().get( 0 ), "/users/roles", "User roles", "/users/roles" );
		verify( userManagement.getItems().get( 1 ), "/users/users", "Users", "/users/users" );

		// Moving back should be possible, builder itself should not have been modified by previous build
		builder.undoMove( "/loggers" );

		menu = builder.build();
		assertEquals( 2, menu.size() );

		administration = menu.getItems().get( 0 );
		verify( administration, "/administration", "Administration", "/administration" );
		assertEquals( 2, administration.size() );

		verify( administration.getItems().get( 0 ), "/administration/system-info", "System info",
		        "/administration/system-info" );

		userManagement = administration.getItems().get( 1 );
		verify( userManagement, "/users", "User management", "/users" );
		verify( userManagement.getItems().get( 0 ), "/users/roles", "User roles", "/users/roles" );
		verify( userManagement.getItems().get( 1 ), "/users/users", "Users", "/users/users" );

		verify( menu.getItems().get( 1 ), "/loggers", "Loggers", "/loggers" );
	}

	@Test
	public void itemProcessors() {
		builder = new PathBasedMenuBuilder( new PrefixTitleProcessor( "processed:" ) );

		builder.item( "test1", "One", "urlOne" )
		       .and()
		       .builder( new PrefixTitleProcessor( "prefixed:" ) )
		       .item( "test2", "Two", "urlTwo" )
		       .and()
		       .item( "test3", "Three", "urlThree" )
		       .and().and()
		       .item( "test4", "Four", "urlFour" );

		Menu menu = builder.build();
		assertEquals( 4, menu.size() );

		assertEquals( "processed:One", menu.getItemWithPath( "test1" ).getTitle() );
		assertEquals( "prefixed:Two", menu.getItemWithPath( "test2" ).getTitle() );
		assertEquals( "prefixed:Three", menu.getItemWithPath( "test3" ).getTitle() );
		assertEquals( "processed:Four", menu.getItemWithPath( "test4" ).getTitle() );
	}

	@Test
	public void buildIntoExisting() {
		Menu existing = new Menu( "one" );
		existing.addItem( "two" ).addItem( "three" );

		builder.item( "four" ).and().build( existing.getFirstItem() );

		assertEquals( "one", existing.getPath() );
		assertEquals( "two", existing.getFirstItem().getPath() );
		assertEquals( "four", existing.getFirstItem().getFirstItem().getPath() );
		assertNull( existing.getItemWithPath( "three" ) );
	}

	@Test
	public void mergeIntoExisting() {
		Menu existing = new Menu( "one" );
		existing.addItem( "two" ).addItem( "three" );

		builder.item( "four" ).and().merge( existing.getFirstItem() );

		assertEquals( "one", existing.getPath() );
		assertEquals( "two", existing.getFirstItem().getPath() );
		assertEquals( "three", existing.getFirstItem().getFirstItem().getPath() );
		assertEquals( "four", existing.getFirstItem().getItems().get( 1 ).getPath() );
	}

	private Menu verify( Menu item, String path, String title, String url ) {
		assertNotNull( item );
		assertEquals( path, item.getPath() );
		assertEquals( title, item.getTitle() );
		assertEquals( url, item.getUrl() );

		return item;
	}

	/**
	 * Prefix the menu title with a string.
	 */
	public static class PrefixTitleProcessor implements MenuItemBuilderProcessor
	{
		private final String prefix;

		public PrefixTitleProcessor( String prefix ) {
			this.prefix = prefix;
		}

		@Override
		public Menu process( Menu menu ) {
			menu.setTitle( prefix + menu.getTitle() );
			return menu;
		}
	}
}
