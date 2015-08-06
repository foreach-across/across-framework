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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
public class TestVoidNodeViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleNodeWithoutAttributes() {
		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		renderAndExpect( node, "<hr />" );

		node.setTagName( "input" );
		renderAndExpect( node, "<input />" );
	}

	@Test
	public void nodeWithSimpleAttributes() {
		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		node.setAttribute( "class", "test-class test" );
		node.setAttribute( "data-something", "1236" );

		renderAndExpect( node, "<hr class='test-class test' data-something='1236' />" );
	}

	@Test
	public void attributeManagement() {
		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		node.addAttributes( Collections.singletonMap( "one", "1" ) );
		node.addAttributes( Collections.singletonMap( "two", 2 ) );
		node.removeAttribute( "bla" );

		assertTrue( node.hasAttribute( "one" ) );
		assertTrue( node.hasAttribute( "two" ) );

		renderAndExpect( node, "<hr one='1' two='2' />" );

		node.removeAttribute( "one" );
		assertFalse( node.hasAttribute( "one" ) );
		assertTrue( node.hasAttribute( "two" ) );

		renderAndExpect( node, "<hr two='2' />" );
	}

	@Test
	public void attributeSerialization() {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put( "name", "myname for you" );
		json.put( "age", 34 );
		json.put( "nested", new Attr( "inside", 666 ) );

		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		node.setAttribute( "data-json", json );
		node.setAttribute( "data-extra", new Attr( "extra", 123456789L ) );

		renderAndExpect(
				node,
				"<hr " +
						"data-json='{\"name\":\"myname for you\",\"age\":34,\"nested\":{\"name\":\"inside\",\"time\":666}}' " +
						"data-extra='{\"name\":\"extra\",\"time\":123456789}' " +
						"/>"
		);
	}

	@Test
	public void idGeneration() {
		VoidNodeViewElement one = new VoidNodeViewElement( "hr" );
		one.setHtmlId( "one" );

		VoidNodeViewElement otherOne = new VoidNodeViewElement( "hr" );
		otherOne.setHtmlId( "one" );

		ContainerViewElement list = new ContainerViewElement();
		list.add( one );
		list.add( otherOne );

		renderAndExpect( list,
		                 "<hr id='one' /><hr id='one1' />" );
	}

	@Test
	public void customTemplateChild() {
		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		node.setCustomTemplate( CUSTOM_TEMPLATE );

		renderAndExpect( node, CUSTOM_TEMPLATE_OUTPUT );
	}

	@Test
	public void cssClassAttributes() {
		VoidNodeViewElement node = new VoidNodeViewElement( "hr" );
		node.addCssClass( "test", "one" );

		renderAndExpect( node, "<hr class='test one' />" );

		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "one" ) );
		assertFalse( node.hasCssClass( "other" ) );

		node.removeCssClass( "one" );

		assertTrue( node.hasCssClass( "test" ) );
		assertFalse( node.hasCssClass( "one" ) );
		renderAndExpect( node, "<hr class='test' />" );

		node.addCssClass( "other" );
		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "other" ) );
		renderAndExpect( node, "<hr class='test other' />" );

		node.addCssClass( "other" );
		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "other" ) );
		renderAndExpect( node, "<hr class='test other' />" );
	}

	public static class Attr
	{
		private String name;
		private long time;

		public Attr( String name, long time ) {
			this.name = name;
			this.time = time;
		}

		public String getName() {
			return name;
		}

		public long getTime() {
			return time;
		}
	}
}
