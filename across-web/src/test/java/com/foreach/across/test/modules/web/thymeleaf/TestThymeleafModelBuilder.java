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
package com.foreach.across.test.modules.web.thymeleaf;

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.model.*;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestThymeleafModelBuilder
{
	@Mock
	private IEngineContext context;

	@Mock
	private ViewElementNodeBuilderRegistry registry;

	@Mock
	private IModelFactory modelFactory;

	@Mock
	private IModel model;

	private ThymeleafModelBuilder modelBuilder;

	@Before
	public void before() {
		when( context.getModelFactory() ).thenReturn( modelFactory );
		when( modelFactory.createModel() ).thenReturn( model );

		modelBuilder = new ThymeleafModelBuilder( context, registry );
	}

	@Test
	public void textIsEscapedByDefault() {
		IText text = mock( IText.class );
		when( modelFactory.createText( "some &gt; text" ) ).thenReturn( text );

		modelBuilder.addText( "some > text" );
		verify( model ).add( text );
	}

	@Test
	public void htmlIsNotEscaped() {
		IText text = mock( IText.class );
		when( modelFactory.createText( "some > html" ) ).thenReturn( text );

		modelBuilder.addHtml( "some > html" );
		verify( model ).add( text );
	}

	@Test
	public void textWithEscapingOption() {
		IText text = mock( IText.class );
		when( modelFactory.createText( "some &gt; text" ) ).thenReturn( text );
		IText html = mock( IText.class );
		when( modelFactory.createText( "some > html" ) ).thenReturn( html );

		modelBuilder.addText( "some > text", true );
		modelBuilder.addText( "some > html", false );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( text );
		ordered.verify( model ).add( html );
	}

	@Test
	public void pendingTagIsWrittenAndBalancedIfModelIsBuilt() {
		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "div" ) ).thenReturn( closeElementTag );

		modelBuilder.addOpenElement( "div" );
		verify( modelFactory, never() ).createOpenElementTag( anyString() );
		verify( modelFactory, never() ).createCloseElementTag( anyString() );

		assertSame( model, modelBuilder.createModel() );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void pendingTagIsWrittenAndBalancedWhenClosed() {
		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "div" ) ).thenReturn( closeElementTag );

		modelBuilder.addOpenElement( "div" );
		verify( modelFactory, never() ).createOpenElementTag( anyString() );
		verify( modelFactory, never() ).createCloseElementTag( anyString() );

		modelBuilder.addCloseElement();
		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void pendingTagIsWrittenIfNewTagAdded() {
		IOpenElementTag openDiv = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openDiv );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addOpenElement( "h1" );

		verify( modelFactory, never() )
				.createOpenElementTag( "h1", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false );
		verify( model ).add( openDiv );
		verifyNoMoreInteractions( model );
	}

	@Test
	public void attributeOperationsIfNoOpenTagIsAnIllegalState() {
		assertIllegalState( b -> b.addAttribute( "test", "value" ) );
		assertIllegalState( b -> b.addAttributes( Collections.emptyMap() ) );
		assertIllegalState( b -> b.addAttributeValue( "test", "value" ) );
		assertIllegalState( b -> b.removeAttribute( "test" ) );
		assertIllegalState( b -> b.removeAttributeValue( "test", "value" ) );
	}

	public void assertIllegalState( Consumer<ThymeleafModelBuilder> c ) {
		boolean thrown = false;
		try {
			c.accept( modelBuilder );
		}
		catch ( IllegalStateException ise ) {
			thrown = true;
		}
		assertTrue( thrown );
	}

	@Test
	public void addAttributes() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );
		attributes.put( "attributeThree", Arrays.asList( "four", "five" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttributes( Collections.singletonMap( "attributeThree", Collections.singleton( "six" ) ) );
		modelBuilder.createModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "three" );
		expected.put( "attributeThree", "six" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void addAttribute() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttribute( "attributeTwo", "four", "five" );
		modelBuilder.addAttribute( "attributeThree", "six" );

		modelBuilder.createModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "four five" );
		expected.put( "attributeThree", "six" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void addAttributeValue() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttributeValue( "attributeTwo", "four", "five" );

		modelBuilder.createModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "three four five" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttributeValue() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttributeValue( "attributeTwo", "three", "four" );
		modelBuilder.removeAttributeValue( "attributeThree", "no worries" );
		modelBuilder.removeAttributeValue( "attributeOne", "two" );

		modelBuilder.createModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttribute() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttribute( "attributeTwo" );
		modelBuilder.removeAttribute( "attributeThree" );

		modelBuilder.createModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttributes() {
		Map<String, Collection<String>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttributes();

		modelBuilder.createModel();

		verify( modelFactory ).createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE,
		                                             false );
	}
}
