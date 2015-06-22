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
package com.foreach.across.test.modules.web.ui;

import com.foreach.across.modules.web.ui.ViewElementBuilderContextImpl;
import org.junit.Test;
import org.springframework.ui.ModelMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestViewElementBuilderContextImpl
{
	@Test
	public void withoutParent() {
		ViewElementBuilderContextImpl ctx = new ViewElementBuilderContextImpl();
		assertFalse( ctx.hasAttribute( "test" ) );

		ctx.setAttribute( "test", "one" );
		assertTrue( ctx.hasAttribute( "test" ) );

		assertEquals( "one", ctx.getAttribute( "test" ) );

		assertArrayEquals( new String[] { "test" }, ctx.attributeNames() );
		assertEquals( Collections.singletonMap( "test", "one" ), ctx.attributeMap() );

		ctx.removeAttribute( "test" );
		assertFalse( ctx.hasAttribute( "test" ) );
	}

	@Test
	public void withParent() {
		ModelMap map = new ModelMap( "one", 1 );
		ViewElementBuilderContextImpl ctx = new ViewElementBuilderContextImpl( map );

		assertTrue( ctx.hasAttribute( "one" ) );
		assertFalse( ctx.hasAttribute( "two" ) );

		map.put( "two", 2 );
		assertTrue( ctx.hasAttribute( "two" ) );

		assertEquals( 1, ctx.getAttribute( "one" ) );
		assertEquals( 2, ctx.getAttribute( "two" ) );

		ctx.setAttribute( "two", "two" );
		assertEquals( "two", ctx.getAttribute( "two" ) );
		assertEquals( 2, map.get( "two" ) );

		ctx.setAttribute( "three", 3 );

		assertEquals( 2, map.size() );
		assertFalse( map.containsAttribute( "three" ) );

		assertArrayEquals( new String[] { "one", "three", "two" }, ctx.attributeNames() );

		Map<String, Object> expected = new HashMap<>();
		expected.put( "one", 1 );
		expected.put( "two", "two" );
		expected.put( "three", 3 );
		assertEquals( expected, ctx.attributeMap() );
	}
}
