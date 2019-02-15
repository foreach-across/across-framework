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

import com.foreach.across.modules.web.menu.RequestMenuSelector.LookupPath;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestRequestMenuSelector
{
	@Test
	public void pathParsing() {
		LookupPath lookup = LookupPath.parse( "/one" );
		assertThat( lookup.getPath() ).isEqualTo( "/one" );
		assertThat( lookup.getQueryParameters() ).isEmpty();
		assertThat( lookup.getFragment() ).isEmpty();

		lookup = LookupPath.parse( "http://localhost:8013/one" );
		assertThat( lookup.getPath() ).isEqualTo( "http://localhost:8013/one" );
		assertThat( lookup.getQueryParameters() ).isEmpty();
		assertThat( lookup.getFragment() ).isEmpty();

		lookup = LookupPath.parse( "http://localhost:8013/one?x=y&z=15" );
		assertThat( lookup.getPath() ).isEqualTo( "http://localhost:8013/one" );
		assertThat( lookup.getQueryParameters() ).containsExactly( "x=y", "z=15" );
		assertThat( lookup.getFragment() ).isEmpty();

		lookup = LookupPath.parse( "http://localhost:8013/one/two?x=y&z=15#132" );
		assertThat( lookup.getPath() ).isEqualTo( "http://localhost:8013/one/two" );
		assertThat( lookup.getQueryParameters() ).containsExactly( "x=y", "z=15" );
		assertThat( lookup.getFragment() ).isEqualTo( "132" );

		lookup = LookupPath.parse( null );
		assertThat( lookup.getPath() ).isEqualTo( "" );
		assertThat( lookup.getQueryParameters() ).isEmpty();
		assertThat( lookup.getFragment() ).isEmpty();

		lookup = LookupPath.parse( "" );
		assertThat( lookup.getPath() ).isEqualTo( "" );
		assertThat( lookup.getQueryParameters() ).isEmpty();
		assertThat( lookup.getFragment() ).isEmpty();
	}

	@Test
	public void pathScoring() {
		assertThat( LookupPath.parse( "/one" ).calculateScore( LookupPath.parse( "/two" ) ) ).isEqualTo( LookupPath.NO_MATCH );
		assertThat( LookupPath.parse( "/one" ).calculateScore( LookupPath.parse( "/one" ) ) ).isEqualTo( LookupPath.EXACT_MATCH );
		assertThat( LookupPath.parse( "/one" ).calculateScore( LookupPath.parse( "/one?x=y" ) ) ).isEqualTo( LookupPath.NO_MATCH );
		assertThat( LookupPath.parse( "/one?x=y" ).calculateScore( LookupPath.parse( "/one" ) ) ).isEqualTo( LookupPath.PATH_EQUALS );
		assertThat( LookupPath.parse( "/one?x=y" ).calculateScore( LookupPath.parse( "/one?x=y" ) ) ).isEqualTo( LookupPath.EXACT_MATCH );
		assertThat( LookupPath.parse( "/one/two" ).calculateScore( LookupPath.parse( "/one" ) ) ).isEqualTo( LookupPath.PATH_STARTS_WITH + 4 );
		assertThat( LookupPath.parse( "/one/two" ).calculateScore( LookupPath.parse( "/one?x=y" ) ) ).isEqualTo( LookupPath.NO_MATCH );
		assertThat( LookupPath.parse( "/one/two/three" ).calculateScore( LookupPath.parse( "/one/two?x=y" ) ) ).isEqualTo( LookupPath.NO_MATCH );
		assertThat( LookupPath.parse( "/one/two?x=y" ).calculateScore( LookupPath.parse( "/one?x=y" ) ) ).isEqualTo( LookupPath.NO_MATCH );
		assertThat( LookupPath.parse( "/one?x=y&z=1" ).calculateScore( LookupPath.parse( "/one?x=y" ) ) )
				.isEqualTo( LookupPath.PATH_EQUALS + LookupPath.QUERY_PARAM_MATCH );
		assertThat( LookupPath.parse( "/one?x=y&test=dummy&z=1" ).calculateScore( LookupPath.parse( "/one?z=1&x=y" ) ) )
				.isEqualTo( LookupPath.PATH_EQUALS + 2 * LookupPath.QUERY_PARAM_MATCH );
		assertThat( LookupPath.parse( "/one?x=y&test=dummy&z=1" ).calculateScore( LookupPath.parse( "/one?z=1&x=y&test=other" ) ) )
				.isEqualTo( LookupPath.NO_MATCH );

		// fragment is ignored to determine exact match - as it is usually not sent to the server anyway
		assertThat( LookupPath.parse( "/one?x=y" ).calculateScore( LookupPath.parse( "/one?x=y#5" ) ) )
				.isEqualTo( LookupPath.EXACT_MATCH );
	}

	@Test
	public void simpleSelectByPath() {
		val menu = menu( "/one", "/two" );
		assertSelected( menu, "http://localhost:8103/context/one", "/one" );
		assertSelected( menu, "http://localhost:8103/context/two", "/two" );
		assertSelected( menu, "http://localhost:8103/context/two/One", "/two" );
	}

	@Test
	public void longestPathPrefixIsUsed() {
		val menu = menu( "/one", "/two", "/one/two", "/two/three" );
		assertSelected( menu, "http://localhost:8103/context/one", "/one" );
		assertSelected( menu, "http://localhost:8103/context/two", "/two" );
		assertSelected( menu, "http://localhost:8103/context/one/two", "/one/two" );
		assertSelected( menu, "http://localhost:8103/context/two/three", "/two/three" );

		assertSelected( menu, "http://localhost:8103/context/two/two", "/two" );
		assertSelected( menu, "http://localhost:8103/context/two/threeFour", "/two" );
	}

	@Test
	public void noMatchOnItemsThatRequireQueryParameter() {
		val menu = menu( "/one?x=y" );
		assertSelected( menu, "http://localhost:8103/context/other", null );
		assertSelected( menu, "http://localhost:8103/context/one", null );
		assertSelected( menu, "http://localhost:8103/context/one?a=b", null );
		assertSelected( menu, "http://localhost:8103/context/one?a=b&x=1", null );
		assertSelected( menu, "http://localhost:8103/context/one?a=b&x=y", "/one?x=y" );
	}

	private void assertSelected( Menu menu, String url, String expectedPath ) {
		RequestMenuSelector selector = new RequestMenuSelector(
				url,
				StringUtils.substringAfter( StringUtils.substringAfterLast( url, ":8013/context" ), "?" ),
				StringUtils.substringAfter( url, ":8103/context" )
		);

		Menu selectedItem = selector.find( menu );
		if ( expectedPath == null ) {
			assertThat( selectedItem ).isNull();
		}
		else {
			assertThat( selectedItem ).isNotNull();
			assertThat( selectedItem.getPath() ).isEqualTo( expectedPath );
		}
	}

	private Menu menu( String... paths ) {
		PathBasedMenuBuilder builder = new PathBasedMenuBuilder();

		for ( String path : paths ) {
			builder.item( path );
		}

		return builder.build();
	}

	@Test
	public void selectLongestPrefix() {
		Menu entities = new Menu( "/entities", "Entities" );
		entities.addItem( "/entities/sector", "Sector" )
		        .addItem( "/entities/sector/create", "Create a new sector" );
		entities.addItem( "/entities/sectorRepresentative", "Sector representative" )
		        .addItem( "/entities/sectorRepresentative/create", "Create a sector representative" );

		Menu menu = new Menu();
		menu.addItem( entities );

		RequestMenuSelector selector = new RequestMenuSelector(
				"http://localhost:8103/sector/entities/sectorRepresentative/-100/update",
				"/entities/sectorRepresentative/-100/update",
				"/entities/sectorRepresentative/-100/update?from=goback"
		);

		Menu selected = selector.find( menu );
		assertEquals( "/entities/sectorRepresentative", selected.getPath() );
	}

	@Test
	public void lowestGetsSelected() {
		Menu menu = new Menu( "/entities", "Entities" );
		menu.addItem( "/entities/sector", "Sector" )
		    .addItem( "/entities/sector/child", "Sector child", "/entities/sector" );

		RequestMenuSelector selector = new RequestMenuSelector(
				"http://localhost:8103/entities/sector",
				"/entities/sector",
				"/entities/sector?from=goback"
		);

		Menu selected = selector.find( menu );
		assertEquals( "/entities/sector/child", selected.getPath() );

		selector = new RequestMenuSelector(
				"http://localhost:8103/entities/sector",
				"/entities/sector",
				"/entities/sector"
		);

		selected = selector.find( menu );
		assertEquals( "/entities/sector/child", selected.getPath() );
	}

	@Test
	public void queryParameterOrderDoesNotMatter() {
		Menu entities = new Menu( "/entities", "Entities" );
		entities.addItem( "/entities/sector", "Sector" )
		        .addItem( "/entities/sector?view=x", "Create a new sector" );
		entities.addItem( "/entities/sectorRepresentative?view=y&x=y", "Sector representative" )
		        .addItem( "/entities/sectorRepresentative/create?view=x", "Create a sector representative" );

		Menu menu = new Menu();
		menu.addItem( entities );

		RequestMenuSelector selector = new RequestMenuSelector(
				"http://localhost:8103/sector/entities/sector?x=y&view=x",
				"/entities/sector",
				"/entities/sector?x=y&view=x"
		);
		assertEquals( "/entities/sector?view=x", selector.find( menu ).getPath() );

		selector = new RequestMenuSelector(
				"http://localhost:8103/sector/entities/sectorRepresentative?view=x",
				"/entities/sector",
				"/entities/sector?x=y&view=x"
		);
		assertEquals( "/entities/sector?view=x", selector.find( menu ).getPath() );
	}
}
