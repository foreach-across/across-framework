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
package com.foreach.across.modules.web.resource;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestWebResourceSorter
{
	@Test
	public void sortByOrder() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( null, "0", null, null, null, null ) );
		items.add( new WebResourceReference( null, "3", null, null, 3, null ) );
		items.add( new WebResourceReference( null, "2", null, null, 2, null ) );
		items.add( new WebResourceReference( null, "1", null, null, 1, null ) );
		items.add( new WebResourceReference( null, "4", null, null, null, null ) );
		List<WebResourceReference> sorted = WebResourceSorter.sort( items );
		assertEquals( "0", sorted.get( 0 ).getKey() );
		assertEquals( "1", sorted.get( 1 ).getKey() );
		assertEquals( "2", sorted.get( 2 ).getKey() );
		assertEquals( "3", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}

	@Test
	public void sortByBefore() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( null, "0", "1", null, null, null ) );
		items.add( new WebResourceReference( null, "2", "3", null, null, null ) );
		items.add( new WebResourceReference( null, "3", "4", null, null, null ) );
		items.add( new WebResourceReference( null, "1", "2", null, 1, null ) );
		items.add( new WebResourceReference( null, "4", null, null, null, null ) );
		List<WebResourceReference> sorted = WebResourceSorter.sort( items );
		assertEquals( "0", sorted.get( 0 ).getKey() );
		assertEquals( "1", sorted.get( 1 ).getKey() );
		assertEquals( "2", sorted.get( 2 ).getKey() );
		assertEquals( "3", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}

	@Test
	public void sortByAfter() {
		List<WebResourceReference> items = new LinkedList<>();
		items.add( new WebResourceReference( null, "0", null, null, null, null ) );
		items.add( new WebResourceReference( null, "2", null, "1", null, null ) );
		items.add( new WebResourceReference( null, "3", null, "2", null, null ) );
		items.add( new WebResourceReference( null, "1", null, null, 1, null ) );
		items.add( new WebResourceReference( null, "4", null, "3", null, null ) );
		List<WebResourceReference> sorted = WebResourceSorter.sort( items );
		assertEquals( "0", sorted.get( 0 ).getKey() );
		assertEquals( "1", sorted.get( 1 ).getKey() );
		assertEquals( "2", sorted.get( 2 ).getKey() );
		assertEquals( "3", sorted.get( 3 ).getKey() );
		assertEquals( "4", sorted.get( 4 ).getKey() );
	}
}
