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
package test.support;

import com.foreach.across.core.support.AttributeSupport;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestAttributeSupport
{
	static class Attributes extends AttributeSupport
	{
		public Attributes() {
		}

		public Attributes( Map<String, Object> backingMap ) {
			super( backingMap );
		}
	}

	@Test
	public void generalBehavior() {
		Attributes attributes = new Attributes();
		attributes.setAttribute( "test", "boe" );
		attributes.setAttribute( "test2", 123 );
		attributes.setAttribute( Long.class, 500L );

		assertTrue( attributes.hasAttribute( "test" ) );
		assertTrue( attributes.hasAttribute( "test2" ) );
		assertTrue( attributes.hasAttribute( Long.class ) );
		assertTrue( attributes.hasAttribute( "java.lang.Long" ) );

		assertFalse( attributes.hasAttribute( "dsjklfjsd" ) );
		assertFalse( attributes.hasAttribute( Integer.class ) );

		assertArrayEquals(
				new String[] { "java.lang.Long", "test", "test2" },
				attributes.attributeNames()
		);

		Map<String, Object> map = attributes.attributeMap();
		assertNotNull( map );
		assertEquals( 3, map.size() );
		assertEquals( "boe", map.get( "test" ) );
		assertEquals( 123, map.get( "test2" ) );
		assertEquals( 500L, map.get( "java.lang.Long" ) );

		assertEquals( "boe", attributes.getAttribute( "test" ) );
		assertEquals( Integer.valueOf( 123 ), attributes.getAttribute( "test2", Integer.class ) );
		assertEquals( Long.valueOf( 500L ), attributes.getAttribute( Long.class ) );

		Map<String, Object> other = new HashMap<>();
		other.put( "test2", 999 );
		other.put( "java.lang.String", "hello" );

		attributes.setAttributes( other );

		assertArrayEquals(
				new String[] { "java.lang.Long", "java.lang.String", "test", "test2" },
				attributes.attributeNames()
		);

		assertEquals( 999, attributes.getAttribute( "test2" ) );

		assertEquals( 4, map.size() );
	}

	@Test
	public void customBackingMap() {
		HashMap<String, Object> map = new HashMap<>();
		Attributes attributes = new Attributes( map );

		attributes.setAttribute( "test", "boe" );
		assertTrue( attributes.hasAttribute( "test" ) );

		Map<String, Object> view = attributes.attributeMap();
		assertEquals( 1, view.size() );

		map.clear();

		assertFalse( attributes.hasAttribute( "test" ) );
		assertTrue( view.isEmpty() );
		assertEquals( 0, attributes.attributeNames().length );
	}
}
