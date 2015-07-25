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

public class TestNodeViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleNodeWithoutContentOrAttributes() {
		NodeViewElement node = new NodeViewElement();
		node.setTagName( "div" );

		renderAndExpect( node, "<div></div>" );
	}

	@Test
	public void nodeWithChildren() {
		NodeViewElement node = NodeViewElement.forTag( "ul" );
		node.add( NodeViewElement.forTag( "li" ) );
		node.add( new TextViewElement( "child text" ) );
		node.add( NodeViewElement.forTag( "li" ) );

		renderAndExpect( node, "<ul><li></li>child text<li></li></ul>" );
	}

	@Test
	public void nodeWithAttributes() {
		NodeViewElement node = NodeViewElement.forTag( "div" );
		node.setAttribute( "class", "test-class test" );
		node.setAttribute( "data-something", "1236" );

		renderAndExpect( node, "<div class='test-class test' data-something='1236'></div>" );
	}

	@Test
	public void nestedNodesWithAttributes() {
		NodeViewElement node = NodeViewElement.forTag( "div" );
		node.setAttribute( "class", "some-class" );

		NodeViewElement paragraph = NodeViewElement.forTag( "p" );
		paragraph.setAttribute( "class", "main-paragraph" );
		paragraph.add( new TextViewElement( "paragraph text" ) );

		node.add( paragraph );

		renderAndExpect( node, "<div class='some-class'><p class='main-paragraph'>paragraph text</p></div>" );
	}

	@Test
	public void idGeneration() {
		NodeViewElement one = NodeViewElement.forTag( "div" );
		one.setHtmlId( "one" );

		NodeViewElement otherOne = NodeViewElement.forTag( "div" );
		otherOne.setHtmlId( "one" );
		one.add( otherOne );
		one.add( otherOne );

		renderAndExpect( one,
		                 "<div id='one'>" +
				                 "<div id='one1'></div>" +
				                 "<div id='one1'></div>" +
				                 "</div>" );
	}

	@Test
	public void customTemplateChild() {
		NodeViewElement node = NodeViewElement.forTag( "div" );
		node.add( new TemplateViewElement( CUSTOM_TEMPLATE ) );

		renderAndExpect( node, "<div>" + CUSTOM_TEMPLATE_OUTPUT + "</div>" );
	}
}
