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
import org.junit.jupiter.api.Test;

public class TestTextViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void emptyText() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( TextViewElement.text( "start" ) );
		container.addChild( TextViewElement.text( null ) );
		container.addChild( TextViewElement.html( null ) );
		container.addChild( TextViewElement.text( "stop" ) );

		renderAndExpect( container, "startstop" );
	}

	@Test
	public void nonHtmlText() {
		renderAndExpect(
				new TextViewElement( "<strong>simple</strong> text" ),
				"&lt;strong&gt;simple&lt;/strong&gt; text"
		);

		renderAndExpect(
				TextViewElement.text( "<strong>simple</strong> text" ),
				"&lt;strong&gt;simple&lt;/strong&gt; text"
		);
	}

	@Test
	public void htmlText() {
		renderAndExpect(
				new TextViewElement( "<strong>test</strong> text", false ),
				"<strong>test</strong> text"
		);

		renderAndExpect(
				TextViewElement.html( "<strong>test</strong> text" ),
				"<strong>test</strong> text"
		);

		renderAndExpect(
				TextViewElement.xml( "<strong>test</strong> text" ),
				"<strong>test</strong> text"
		);
	}

	@Test
	public void customTemplateWithoutFragment() {
		TextViewElement text = new TextViewElement( "text content" );
		text.setCustomTemplate( "th/test/elements/text" );

		renderAndExpect( text, "<h3 class=\"page-header\">text content</h3>" );
	}

	@Test
	public void customTemplateWithFragmentButNoVariables() {
		TextViewElement text = new TextViewElement( "text content" );
		text.setCustomTemplate( "th/test/elements/text :: randomText" );

		renderAndExpect( text, "Some random text instead..." );
	}

	@Test
	public void customTemplateWithFragmendAndVariables() {
		TextViewElement text = new TextViewElement( "text content" );
		text.setCustomTemplate( "th/test/elements/text :: otherTemplate(${component})" );

		renderAndExpect( text, "<div>Received text: text content</div>" );
	}

	@Test
	public void nestedTemplateRendering() {
		TextViewElement text = new TextViewElement( "initial text" );
		text.setCustomTemplate( "th/test/elements/text :: nestedTemplate(${component})" );

		TextViewElement other = new TextViewElement( "other text" );
		other.setCustomTemplate( "th/test/elements/text :: otherTemplate(${component})" );

		renderAndExpect( text, ( model ) -> model.addAttribute( "otherElement", other ),
		                 "initial text: <div>Received text: other text</div>" );
	}
}
