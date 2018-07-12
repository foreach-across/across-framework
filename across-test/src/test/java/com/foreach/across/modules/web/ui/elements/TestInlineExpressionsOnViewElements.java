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

/**
 * Tests for AX-218 - inline expressions being parsed in view elements.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestInlineExpressionsOnViewElements extends AbstractViewElementTemplateTest
{
	@Test
	public void inlineExpressionInCustomViewElementShouldBeEvaluated() {
		TextViewElement text = new TextViewElement( "hello" );
		text.setCustomTemplate( "th/test/elements/text :: manualText" );

		renderAndExpect( text, "<p>hello</p>" );
	}

	@Test
	public void inlineExpressionsShouldNotBeEvaluatedInsideViewElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.addChild( TextViewElement.text( "[[1/0]]" ) );
		container.addChild( TextViewElement.html( "[[test]]" ) );

		renderAndExpect( container, "[[1/0]][[test]]" );
	}

	@Test
	public void inlineExpressionsInTemplateAndCustomTemplatesShouldBeEvaluated() {
		setTemplate( "th/webControllers/renderViewElementWithExpressions" );

		TextViewElement withNested = new TextViewElement( "initial text" );
		withNested.setCustomTemplate( "th/test/elements/text :: nestedTemplate(${component})" );

		ContainerViewElement childContainer = new ContainerViewElement();
		childContainer.addChild( TextViewElement.html( "[[33/0]]" ) );
		childContainer.addChild( new TextViewElement( "other text" ) );

		ContainerViewElement container = new ContainerViewElement();
		container.addChild( TextViewElement.text( "[[1/0]]" ) );
		container.addChild( TextViewElement.html( "<div>" ) );
		container.addChild( withNested );
		container.addChild( TextViewElement.html( "</div>" ) );
		container.addChild( TextViewElement.html( "[[test]]" ) );

		renderAndExpect( container, ( model ) -> model.addAttribute( "otherElement", childContainer ),
		                 "unwrapped[[1/0]]<div>" +
				                 "initial text: [[33/0]]other text</div>[[test]]3" );
	}
}
