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

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = TestViewElementNameRendering.Config.class)
public class TestViewElementNameRendering extends AbstractViewElementTemplateTest
{
	@Test
	public void simpleNodeWithoutContentOrAttributes() {
		NodeViewElement node = new NodeViewElement( "div" );

		renderAndExpect( node, "<div></div>" );
	}

	@Test
	public void nodeWithChildren() {
		NodeViewElement node = new NodeViewElement( "ul" );
		node.setName( "parent" );

		NodeViewElement li = new NodeViewElement( "li" );
		li.setName( "child" );
		li.setAttribute( "test", "valueIgnoringLineEndings" );
		node.addChild( li );

		TextViewElement text = new TextViewElement( "child text" );
		text.setName( "text" );
		node.addChild( text );

		node.addChild( new NodeViewElement( "li" ) );

		renderAndExpect( node, "<ul data-ax-dev-view-element='parent'>" +
				"<li test='valueIgnoringLineEndings' data-ax-dev-view-element='child'></li>" +
				"child text" +
				"<li></li></ul>" );
	}

	@Test
	public void nodeWithSingleVisibleChild() {
		TextViewElement text = new TextViewElement( "my text" );
		text.setName( "my text view element" );

		assertEquals( "<!--[ax:my text view element]-->my text<!--[/ax:my text view element]-->", render( text ) );
	}

	@Test
	public void nestedContainerElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.setName( "containerName" );
		container.addChild( new TextViewElement( "one, " ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.setName( "subContainerName" );
		subContainer.addChild( new TextViewElement( "two, " ) );

		NodeViewElement node = new NodeViewElement( "b" );
		node.setName( "bold" );
		node.addChild( TextViewElement.text( "bold text" ) );
		subContainer.addChild( node );

		container.addChild( subContainer );
		assertEquals(
				"<!--[ax:containerName]-->one, " +
						"<!--[ax:subContainerName]-->two, " +
						"<!--[ax:bold]--><b data-ax-dev-view-element=\"bold\">bold text</b><!--[/ax:bold]-->" +
						"<!--[/ax:subContainerName]-->" +
						"<!--[/ax:containerName]-->",
				render( container )
		);
	}

	@Test
	public void customTemplateChild() {
		NodeViewElement node = new NodeViewElement( "div" );
		node.setCustomTemplate( CUSTOM_TEMPLATE );
		assertEquals( CUSTOM_TEMPLATE_OUTPUT, render( node ).trim() );

		node.setName( "custom" );
		assertEquals(
				"<!--[ax:custom]-->" + CUSTOM_TEMPLATE_OUTPUT + "<!--[/ax:custom]-->",
				render( node ).trim()
		);

		NodeViewElement parent = new NodeViewElement( "div" );
		parent.addChild( node );
		assertEquals(
				"<div><!--[ax:custom]-->" + CUSTOM_TEMPLATE_OUTPUT + "<!--[/ax:custom]--></div>",
				render( parent ).trim()
		);
	}

	@Configuration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.setDevelopmentMode( true );
		}
	}
}
