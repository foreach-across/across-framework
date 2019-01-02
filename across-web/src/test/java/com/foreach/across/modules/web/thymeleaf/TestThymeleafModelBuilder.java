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
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.model.*;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;
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
	private ViewElementModelWriterRegistry registry;

	@Mock
	private HtmlIdStore htmlIdStore;

	@Mock
	private ViewElementAttributeConverter attributeConverter;

	@Mock
	private IModelFactory modelFactory;

	@Mock
	private IModel model;

	private ThymeleafModelBuilder modelBuilder;

	@Before
	public void before() {
		when( context.getModelFactory() ).thenReturn( modelFactory );
		when( modelFactory.createModel() ).thenReturn( model );
		doAnswer( invocation -> invocation.getArgument( 0 ) )
				.when( attributeConverter ).apply( any() );

		when( context.getTemplateMode() ).thenReturn( TemplateMode.HTML );

		modelBuilder = new ThymeleafModelBuilder( context, registry, htmlIdStore, attributeConverter, new AttributeNameGenerator(), false );
	}

	@Test
	public void nullStringIsIgnoredAsText() {
		modelBuilder.addText( null );
		modelBuilder.addHtml( null );
		modelBuilder.addText( null, true );
		modelBuilder.addText( null, false );

		verify( modelFactory, never() ).createText( anyString() );
		verify( model, never() ).add( any( ITemplateEvent.class ) );
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
	public void pendingTagIsWrittenIfModelIsBuilt() {
		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );

		modelBuilder.addOpenElement( "div" );
		verify( modelFactory, never() ).createOpenElementTag( anyString() );
		verify( modelFactory, never() ).createCloseElementTag( anyString() );

		assertSame( model, modelBuilder.retrieveModel() );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		verify( modelFactory, never() ).createCloseElementTag( anyString() );
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
		when( modelFactory.createOpenElementTag( "div", Collections.singletonMap( "one", "value" ),
		                                         AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openDiv );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttribute( "one", "value" );
		modelBuilder.addOpenElement( "h1" );

		verify( modelFactory, never() )
				.createOpenElementTag( "h1", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false );
		verify( model ).add( openDiv );
		verifyNoMoreInteractions( model );

		modelBuilder.retrieveModel();
		verify( modelFactory )
				.createOpenElementTag( "h1", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void multipleTags() {
		IOpenElementTag openDiv = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openDiv );
		IOpenElementTag openHeading = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "h1", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openHeading );
		ICloseElementTag closeDiv = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "div" ) ).thenReturn( closeDiv );
		ICloseElementTag closeHeading = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "h1" ) ).thenReturn( closeHeading );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addOpenElement( "h1" );
		modelBuilder.addCloseElement();
		modelBuilder.addCloseElement();

		modelBuilder.retrieveModel();

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openDiv );
		ordered.verify( model ).add( openHeading );
		ordered.verify( model ).add( closeHeading );
		ordered.verify( model ).add( closeDiv );
	}

	@Test
	public void openElementCanBeChanged() {
		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "h1", Collections.singletonMap( "one", "value" ),
		                                         AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "h1" ) ).thenReturn( closeElementTag );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttribute( "one", "value" );
		modelBuilder.changeOpenElement( "h1" );
		modelBuilder.addCloseElement();

		modelBuilder.retrieveModel();

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void changeThrowsIllegalStateIfNoOpenElement() {
		modelBuilder.addOpenElement( "div" );
		modelBuilder.addText( "flush open element" );
		assertIllegalState( b -> b.changeOpenElement( "h1" ) );
	}

	@Test
	public void closeThrowsIllegalStateIfNoOpenElement() {
		assertIllegalState( ThymeleafModelBuilder::addCloseElement );
	}

	@Test
	public void attributeOperationsIfNoOpenTagIsAnIllegalState() {
		assertIllegalState( b -> b.addAttribute( "test", "value" ) );
		assertIllegalState( b -> b.addAttributes( Collections.emptyMap() ) );
		assertIllegalState( b -> b.addAttributeValue( "test", "value" ) );
		assertIllegalState( b -> b.addBooleanAttribute( "required", false ) );
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
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );
		attributes.put( "attributeThree", Arrays.asList( "four", "five" ) );
		attributes.put( "noValuesAreIgnored", Collections.emptyList() );
		attributes.put( "nullValuesAreIgnored", Collections.singleton( null ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttributes( Collections.singletonMap( "attributeThree", Collections.singleton( "six" ) ) );
		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "three" );
		expected.put( "attributeThree", "six" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void addAttribute() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );
		attributes.put( "attributeFour", Collections.singleton( "four" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttribute( "attributeTwo", "four", "five" );
		modelBuilder.addAttribute( "attributeThree", "six" );
		modelBuilder.addAttribute( "nullValueIsIgnored", (String) null );
		modelBuilder.addAttribute( "attributeFour", (String) null );
		modelBuilder.addAttribute( "symmetric" );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "four five" );
		expected.put( "attributeThree", "six" );
		expected.put( "symmetric", "symmetric" );
		expected.put( "attributeFour", "four" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void addAttributeValue() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttributeValue( "attributeTwo", "four", "five" );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		expected.put( "attributeTwo", "three four five" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttributeValue() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttributeValue( "attributeTwo", "three", "four" );
		modelBuilder.removeAttributeValue( "attributeThree", "no worries" );
		modelBuilder.removeAttributeValue( "attributeOne", "two" );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttribute() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttribute( "attributeTwo" );
		modelBuilder.removeAttribute( "attributeThree" );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one two" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void removeAttributes() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one", "two" ) );
		attributes.put( "attributeTwo", Collections.singleton( "three" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.removeAttributes();

		modelBuilder.retrieveModel();

		verify( modelFactory )
				.createOpenElementTag( "div", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void attributeValuesShouldBeEscaped() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "attributeOne", Arrays.asList( "one >", "< two" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addAttribute( "attributeTwo", "two \"" );
		modelBuilder.addAttributeValue( "attributeTwo", "< three" );
		modelBuilder.removeAttributeValue( "attributeOne", "< two" );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "attributeOne", "one &gt;" );
		expected.put( "attributeTwo", "two &quot; &lt; three" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void addBooleanAttribute() {
		Map<String, Collection<Object>> attributes = new HashMap<>();
		attributes.put( "replacedAsBoolean", Arrays.asList( "one", "two" ) );
		attributes.put( "removedAsBoolean", Arrays.asList( "one", "two" ) );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttributes( attributes );
		modelBuilder.addBooleanAttribute( "addedAsBoolean", true );
		modelBuilder.addBooleanAttribute( "notAddedAsBoolean", false );
		modelBuilder.addBooleanAttribute( "replacedAsBoolean", true );
		modelBuilder.addBooleanAttribute( "removedAsBoolean", false );

		modelBuilder.retrieveModel();

		Map<String, String> expected = new HashMap<>();
		expected.put( "replacedAsBoolean", "replacedAsBoolean" );
		expected.put( "addedAsBoolean", "addedAsBoolean" );
		verify( modelFactory ).createOpenElementTag( "div", expected, AttributeValueQuotes.DOUBLE, false );
	}

	@Test
	public void retrieveHtmlId() {
		ViewElement ve = mock( ViewElement.class );
		when( htmlIdStore.retrieveHtmlId( context, ve ) ).thenReturn( "123" );

		String id = modelBuilder.retrieveHtmlId( ve );
		assertEquals( "123", id );
	}

	@Test
	public void customTemplateWithoutFragment() {
		ViewElement ve = mock( ViewElement.class );
		when( ve.getCustomTemplate() ).thenReturn( "myTemplate" );

		Map<String, String> attributes = new HashMap<>( 2 );
		attributes.put( "th:insert", "myTemplate :: render(component=${_generatedAttribute0})" );
		attributes.put( "th:inline", TemplateMode.HTML.name());

		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "th:block", attributes, AttributeValueQuotes.DOUBLE,false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "th:block" ) ).thenReturn( closeElementTag );

		modelBuilder.addViewElement( ve );

		modelBuilder.retrieveModel();
		verify( context ).setVariable( "_generatedAttribute0", ve );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void customTemplateWithFragmentAppended() {
		ViewElement ve = mock( ViewElement.class );
		when( ve.getCustomTemplate() ).thenReturn( "myTemplate :: customFragment" );

		Map<String, String> attributes = new HashMap<>( 2 );
		attributes.put( "th:insert", "myTemplate :: customFragment(component=${_generatedAttribute0})" );
		attributes.put( "th:inline", TemplateMode.HTML.name());

		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "th:block", attributes, AttributeValueQuotes.DOUBLE,false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "th:block" ) ).thenReturn( closeElementTag );

		modelBuilder.addViewElement( ve );

		modelBuilder.retrieveModel();
		verify( context ).setVariable( "_generatedAttribute0", ve );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void renderCustomTemplateShouldFlushTag() {
		ViewElement ve = mock( ViewElement.class );
		when( ve.getCustomTemplate() ).thenReturn( "myTemplate :: customFragment" );

		IOpenElementTag openHeader = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "h1", Collections.emptyMap(), AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openHeader );

		Map<String, String> attributes = new HashMap<>( 2 );
		attributes.put( "th:insert", "myTemplate :: customFragment(component=${_generatedAttribute0})" );
		attributes.put( "th:inline", TemplateMode.HTML.name());

		IOpenElementTag openElementTag = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "th:block", attributes, AttributeValueQuotes.DOUBLE,false ) )
				.thenReturn( openElementTag );
		ICloseElementTag closeElementTag = mock( ICloseElementTag.class );
		when( modelFactory.createCloseElementTag( "th:block" ) ).thenReturn( closeElementTag );

		modelBuilder.addOpenElement( "h1" );
		modelBuilder.addViewElement( ve );

		modelBuilder.retrieveModel();
		verify( context ).setVariable( "_generatedAttribute0", ve );

		InOrder ordered = inOrder( model );
		ordered.verify( model ).add( openHeader );
		ordered.verify( model ).add( openElementTag );
		ordered.verify( model ).add( closeElementTag );
	}

	@Test
	public void nullViewElementIsIgnored() {
		modelBuilder.addViewElement( null );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void writeViewElement() {
		ViewElement ve = mock( ViewElement.class );
		ViewElementModelWriter veWriter = mock( ViewElementModelWriter.class );
		when( registry.getModelWriter( ve ) ).thenReturn( veWriter );

		modelBuilder.addViewElement( ve );
		verify( veWriter ).writeModel( ve, modelBuilder );
	}

	@Test
	public void addModelWritesPendingTag() {
		IModel otherModel = mock( IModel.class );

		IOpenElementTag openDiv = mock( IOpenElementTag.class );
		when( modelFactory.createOpenElementTag( "div", Collections.singletonMap( "one", "value" ),
		                                         AttributeValueQuotes.DOUBLE, false ) )
				.thenReturn( openDiv );

		modelBuilder.addOpenElement( "div" );
		modelBuilder.addAttribute( "one", "value" );
		modelBuilder.addModel( otherModel );
		modelBuilder.addOpenElement( "h1" );

		InOrder inOrder = inOrder( model );
		inOrder.verify( model ).add( openDiv );
		inOrder.verify( model ).addModel( otherModel );
		verifyNoMoreInteractions( model );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createViewElementModelReturnsNewModel() {
		ViewElement ve = mock( ViewElement.class );
		ViewElementModelWriter veWriter = mock( ViewElementModelWriter.class );
		when( registry.getModelWriter( ve ) ).thenReturn( veWriter );

		IModel childModel = mock( IModel.class );
		when( modelFactory.createModel() ).thenReturn( childModel );

		modelBuilder.addOpenElement( "div" );
		doAnswer( invocation -> {
			          ThymeleafModelBuilder childBuilder = invocation.getArgument( 1 );
			          assertNotSame( modelBuilder, childBuilder );
			          assertSame( context, childBuilder.getTemplateContext() );
			          assertSame( childModel, childBuilder.retrieveModel() );
			          assertSame( modelFactory, childBuilder.getModelFactory() );
			          return null;
		          }
		).when( veWriter ).writeModel( eq( ve ), any( ThymeleafModelBuilder.class ) );

		assertSame( childModel, modelBuilder.createViewElementModel( ve ) );
		verify( veWriter ).writeModel( eq( ve ), any( ThymeleafModelBuilder.class ) );
		verifyNoMoreInteractions( model );
	}
}
