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

import com.foreach.across.core.support.AttributeOverridingSupport;
import com.foreach.across.core.support.AttributeSupport;
import com.foreach.across.core.support.InheritedAttributeValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestAttributeOverridingSupport
{
	@Test
	public void generalBehaviorWithoutParent() {
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

		// Previous map should not get modified
		assertEquals( 3, map.size() );
		assertEquals( 4, attributes.attributeMap().size() );

		assertAttributeValue( attributes.findAttribute( "nonExisting" ), "nonExisting", null, -1 );
		assertAttributeValue( attributes.findAttribute( "test2", Integer.class ), "test2", 999, 0 );
		assertAttributeValue( attributes.findAttribute( Long.class ), "java.lang.Long", 500L, 0 );
	}

	@Test
	public void inheritedFromParent() {
		ParentAttributes parent = new ParentAttributes();
		Attributes attributes = new Attributes( parent );
		parent.setAttribute( "test", "boe" );
		parent.setAttribute( "test2", 123 );
		parent.setAttribute( Long.class, 500L );

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

		parent.setAttributes( other );

		assertArrayEquals(
				new String[] { "java.lang.Long", "java.lang.String", "test", "test2" },
				attributes.attributeNames()
		);

		assertEquals( 999, attributes.getAttribute( "test2" ) );

		// Previous map should not get modified
		assertEquals( 3, map.size() );
		assertEquals( 4, attributes.attributeMap().size() );

		assertAttributeValue( attributes.findAttribute( "nonExisting" ), "nonExisting", null, -1 );
		assertAttributeValue( attributes.findAttribute( "test" ), "test", "boe", 1 );
		assertAttributeValue( attributes.findAttribute( "test2", Integer.class ), "test2", 999, 1 );
		assertAttributeValue( attributes.findAttribute( Long.class ), "java.lang.Long", 500L, 1 );
		assertAttributeValue( attributes.findAttribute( String.class ), "java.lang.String", "hello", 1 );
	}

	@Test
	public void overrideFromParent() {
		ParentAttributes parent = new ParentAttributes();
		Attributes attributes = new Attributes( parent );
		parent.setAttribute( "test", "boe" );
		parent.setAttribute( "test2", 123 );
		parent.setAttribute( Long.class, 500L );

		attributes.setAttribute( "test2", "ba" );
		attributes.setAttribute( Long.class, null );

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
		assertEquals( "ba", map.get( "test2" ) );
		assertNull( map.get( "java.lang.Long" ) );

		assertEquals( "boe", attributes.getAttribute( "test" ) );
		assertEquals( "ba", attributes.getAttribute( "test2", String.class ) );
		assertNull( attributes.getAttribute( Long.class ) );

		attributes.removeAttribute( "test2" );
		assertEquals( Integer.valueOf( 123 ), attributes.getAttribute( "test2", Integer.class ) );

		Map<String, Object> other = new HashMap<>();
		other.put( "test2", 999 );
		other.put( "java.lang.String", "hello" );

		attributes.setAttributes( other );

		assertArrayEquals(
				new String[] { "java.lang.Long", "java.lang.String", "test", "test2" },
				attributes.attributeNames()
		);
		assertArrayEquals(
				new String[] { "java.lang.Long", "test", "test2" },
				parent.attributeNames()
		);

		assertEquals( 999, attributes.getAttribute( "test2" ) );
		assertEquals( 123, parent.getAttribute( "test2" ) );

		// Previous map should not get modified
		assertEquals( 3, parent.attributeMap().size() );
		assertEquals( 4, attributes.attributeMap().size() );

		assertAttributeValue( attributes.findAttribute( "nonExisting" ), "nonExisting", null, -1 );
		assertAttributeValue( attributes.findAttribute( "test" ), "test", "boe", 1 );
		assertAttributeValue( attributes.findAttribute( "test2", Integer.class ), "test2", 999, 0 );
		assertAttributeValue( attributes.findAttribute( Long.class ), "java.lang.Long", null, 0 );
		assertAttributeValue( attributes.findAttribute( String.class ), "java.lang.String", "hello", 0 );
	}

	private <U> void assertAttributeValue( InheritedAttributeValue<U> value, String name, U actualValue, int ancestorLevel ) {
		assertNotNull( value );
		assertEquals( name, value.getAttributeName() );
		assertEquals( ancestorLevel, value.getAncestorLevel() );
		assertEquals( actualValue, value.getValue() );
	}

	static class Attributes extends AttributeOverridingSupport
	{
		public Attributes() {
		}

		public Attributes( ParentAttributes parent ) {
			super( parent );
		}
	}

	static class ParentAttributes extends AttributeSupport
	{
		public ParentAttributes() {
		}
	}
}
