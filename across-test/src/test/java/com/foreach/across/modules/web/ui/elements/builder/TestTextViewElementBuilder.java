/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TestTextViewElementBuilder extends AbstractViewElementBuilderTest<TextViewElementBuilder, TextViewElement>
{
	@Override
	protected TextViewElementBuilder createBuilder( ViewElementBuilderFactory factory ) {
		return new TextViewElementBuilder();
	}

	@Test
	public void defaults() {
		build();

		assertNull( element.getText() );
		assertTrue( element.isEscapeXml() );
	}

	@Test
	public void text() {
		builder.escapeXml( false ).text( "some text" );

		build();

		assertEquals( "some text", element.getText() );
		assertTrue( element.isEscapeXml() );
	}

	@Test
	public void textResolving() {
		builder.text( "#{my.text=Hello}" );

		build();
		assertEquals( "Hello", element.getText() );
	}

	@Test
	public void html() {
		builder.escapeXml( true ).html( "some text" );

		build();

		assertEquals( "some text", element.getText() );
		assertFalse( element.isEscapeXml() );
	}

	@Test
	public void xml() {
		builder.escapeXml( true ).xml( "some text" );

		build();

		assertEquals( "some text", element.getText() );
		assertFalse( element.isEscapeXml() );
	}

	@Test
	public void content() {
		builder.escapeXml( false ).content( "some text" );
		build();
		assertEquals( "some text", element.getText() );
		assertFalse( element.isEscapeXml() );

		reset();

		builder.escapeXml( true ).content( "some text" );
		build();
		assertEquals( "some text", element.getText() );
		assertTrue( element.isEscapeXml() );
	}

	@Test
	public void escapeXmlSpecified() {
		builder.escapeXml( false );

		build();

		assertFalse( element.isEscapeXml() );
	}

	@Test
	public void customSupplier() {
		Assertions.assertThat( builder.elementSupplier( () -> new TextViewElement( "hello" ) ) ).isInstanceOf( TextViewElementBuilder.class );
		build();
		assertEquals( "hello", element.getText() );
	}

	@Test
	public void customSupplierFunction() {
		when( builderContext.getMessage( "say.hello" ) ).thenReturn( "hello!" );

		Assertions.assertThat( builder.elementSupplier( bc -> new TextViewElement( bc.getMessage( "say.hello" ) ) ) ).isInstanceOf(
				TextViewElementBuilder.class );
		build();
		assertEquals( "hello!", element.getText() );
	}
}
