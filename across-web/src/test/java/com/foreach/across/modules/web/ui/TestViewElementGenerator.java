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
package com.foreach.across.modules.web.ui;

import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TestViewElementGenerator
{
	private final List<String> data = Arrays.asList( "one", "two", "three" );

	private ViewElementGenerator<String, Text> generator;

	@Before
	public void reset() {
		generator = new ViewElementGenerator<>();
		generator.setItems( data );
	}

	@Test
	public void notEmpty() {
		assertFalse( generator.isEmpty() );
		assertEquals( 3, generator.size() );
	}

	@Test
	public void empty() {
		generator.setItems( Collections.<String>emptyList() );

		assertTrue( generator.isEmpty() );
		assertEquals( 0, generator.size() );
	}

	@Test
	public void elementTemplateRepeatedGenerationWithoutCallback() {
		Text template = new Text();
		template.setText( "template" );

		generator.setItemTemplate( template );

		assertEquals(
				Arrays.asList( "template", "template", "template" ),
				generate()
		);

		assertEquals(
				Arrays.asList( "template", "template", "template" ),
				generate()
		);
	}

	@Test
	public void elementTemplateRepeatedGeneration() {
		Text template = new Text();
		template.setText( "template" );

		generator.setItemTemplate( template );
		generator.setCreationCallback( new ViewElementGenerator.CreationCallback<String, Text>()
		{
			@Override
			public Text create( IteratorItemStats<String> item, Text template ) {
				template.setText( item.getItem() );
				return template;
			}
		} );

		assertEquals(
				Arrays.asList( "one", "two", "three" ),
				generate()
		);

		assertEquals(
				Arrays.asList( "one", "two", "three" ),
				generate()
		);

		generator.setCreationCallback( null );

		assertEquals(
				Arrays.asList( "three", "three", "three" ),
				generate()
		);
	}

	@Test
	public void builderTemplateRepeatedGeneration() {
		generator.setItemTemplate( new TextBuilder() );

		assertEquals(
				Arrays.asList( "1", "2", "3" ),
				generate()
		);

		// Same results should be returned
		assertEquals(
				Arrays.asList( "1", "2", "3" ),
				generate()
		);
	}

	@Test
	public void builderTemplateWithCallback() {
		generator.setItemTemplate( new TextBuilder() );
		generator.setCreationCallback( new ViewElementGenerator.CreationCallback<String, Text>()
		{
			@Override
			public Text create( IteratorItemStats<String> itemStats, Text template ) {
				template.setText( template.getText() + "-" + itemStats.getItem() );
				return template;
			}
		} );

		assertEquals(
				Arrays.asList( "1-one", "2-two", "3-three" ),
				generate()
		);

		// Same results should be returned
		assertEquals(
				Arrays.asList( "1-one", "2-two", "3-three" ),
				generate()
		);
	}

	@Test
	@SuppressWarnings("all")
	public void noTemplateAndNoCallbackResultsInNullElements() {
		assertEquals( Arrays.asList( null, null, null ), generate() );
	}

	private List<String> generate() {
		List<String> texts = new ArrayList<>();

		for ( Text textViewElement : generator ) {
			if ( textViewElement == null ) {
				texts.add( null );
			}
			else {
				texts.add( textViewElement.getText() );
			}
		}

		return texts;
	}

	private static class Text extends ViewElementSupport
	{
		private String text;

		public Text() {
			super( "test-text" );
		}

		public String getText() {
			return text;
		}

		public void setText( String text ) {
			this.text = text;
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			Text text1 = (Text) o;

			if ( text != null ? !text.equals( text1.text ) : text1.text != null ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return text != null ? text.hashCode() : 0;
		}
	}

	private static class TextBuilder implements ViewElementBuilder<Text>
	{
		private int count = 0;

		@Override
		public Text build( ViewElementBuilderContext builderContext ) {
			Text t = new Text();
			t.setText( "" + ++count );

			return t;
		}
	}
}
