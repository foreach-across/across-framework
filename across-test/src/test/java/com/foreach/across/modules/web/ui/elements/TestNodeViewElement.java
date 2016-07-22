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

public class TestNodeViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleNodeWithoutContentOrAttributes() {
		NodeViewElement node = new NodeViewElement( "div" );

		renderAndExpect( node, "<div></div>" );
	}

	@Test
	public void nodeWithChildren() {
		NodeViewElement node = new NodeViewElement( "ul" );
		node.addChild( new NodeViewElement( "li" ) );
		node.addChild( new TextViewElement( "child text" ) );
		node.addChild( new NodeViewElement( "li" ) );

		renderAndExpect( node, "<ul><li></li>child text<li></li></ul>" );
	}

	@Test
	public void nodeWithAttributes() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.setAttribute( "class", "test-class test" );
		node.setAttribute( "data-something", "1236" );

		renderAndExpect( node, "<div class='test-class test' data-something='1236'></div>" );
	}

	@Test
	public void attributeManagement() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.addAttributes( Collections.singletonMap( "one", "1" ) );
		node.addAttributes( Collections.singletonMap( "two", 2 ) );
		node.removeAttribute( "bla" );

		assertTrue( node.hasAttribute( "one" ) );
		assertTrue( node.hasAttribute( "two" ) );

		renderAndExpect( node, "<div one='1' two='2' />" );

		node.removeAttribute( "one" );
		assertFalse( node.hasAttribute( "one" ) );
		assertTrue( node.hasAttribute( "two" ) );

		renderAndExpect( node, "<div two='2' />" );
	}

	@Test
	public void attributeSerialization() {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put( "name", "myname for you" );
		json.put( "age", 34 );
		json.put( "nested", new TestVoidNodeViewElement.Attr( "inside", 666 ) );

		NodeViewElement node = new NodeViewElement( "div" );
		node.setAttribute( "data-json", json );
		node.setAttribute( "data-extra", new TestVoidNodeViewElement.Attr( "extra", 123456789L ) );

		renderAndExpect(
				node,
				"<div " +
						"data-json='{\"name\":\"myname for you\",\"age\":34,\"nested\":{\"name\":\"inside\",\"time\":666}}' " +
						"data-extra='{\"name\":\"extra\",\"time\":123456789}' " +
						"/>"
		);
	}

	@Test
	public void nestedNodesWithAttributes() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.setAttribute( "class", "some-class" );

		NodeViewElement paragraph = new NodeViewElement( "p" );
		paragraph.setAttribute( "class", "main-paragraph" );
		paragraph.addChild( new TextViewElement( "paragraph text" ) );

		node.addChild( paragraph );

		renderAndExpect( node, "<div class='some-class'><p class='main-paragraph'>paragraph text</p></div>" );
	}

	@Test
	public void idGeneration() {
		NodeViewElement one = new NodeViewElement( "div" );
		one.setHtmlId( "one" );

		NodeViewElement otherOne = new NodeViewElement( "div" );
		otherOne.setHtmlId( "one" );
		one.addChild( otherOne );
		one.addChild( otherOne );

		renderAndExpect( one,
		                 "<div id='one'>" +
				                 "<div id='one1'></div>" +
				                 "<div id='one1'></div>" +
				                 "</div>" );
	}

	@Test
	public void customTemplateChild() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.addChild( new TemplateViewElement( CUSTOM_TEMPLATE ) );

		renderAndExpect( node, "<div>" + CUSTOM_TEMPLATE_OUTPUT + "</div>" );
	}

	@Test
	public void cssClassAttributes() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.addCssClass( "test", "one" );

		renderAndExpect( node, "<div class='test one' />" );

		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "one" ) );
		assertFalse( node.hasCssClass( "other" ) );

		node.removeCssClass( "one" );

		assertTrue( node.hasCssClass( "test" ) );
		assertFalse( node.hasCssClass( "one" ) );
		renderAndExpect( node, "<div class='test' />" );

		node.addCssClass( "other" );
		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "other" ) );
		renderAndExpect( node, "<div class='test other' />" );

		node.addCssClass( "other" );
		assertTrue( node.hasCssClass( "test" ) );
		assertTrue( node.hasCssClass( "other" ) );
		renderAndExpect( node, "<div class='test other' />" );
	}
}
