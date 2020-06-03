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
package com.foreach.across.modules.web.ui;

import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.1.1
 */
@ExtendWith(MockitoExtension.class)
class TestViewElementBuilder
{
	@Mock
	private ViewElementBuilderContext threadLocal;

	@Mock
	private ViewElementBuilderContext requestBound;

	@BeforeEach
	@AfterEach
	void reset() {
		RequestContextHolder.resetRequestAttributes();
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Test
	void builderFromSupplier() {
		ViewElementBuilder<NodeViewElement> builder = ViewElementBuilder.of( () -> new NodeViewElement( "div" ) );
		assertElementOf( builder ).isEqualTo( new NodeViewElement( "div" ) );

		assertElementOf(
				builder.postProcess( ( bc, node ) -> node.setAttribute( "one", 1 ) )
				       .postProcess( ( bc, node ) -> node.setAttribute( "two", "two" ) )
		)
				.isEqualTo(
						new NodeViewElement( "div" )
								.setAttribute( "one", 1 )
								.setAttribute( "two", "two" )
				);
	}

	@Test
	void builderFromFunction() {
		ViewElementBuilder<NodeViewElement> builder = ViewElementBuilder.of( builderContext -> {
			assertThat( builderContext ).isSameAs( threadLocal );
			return new NodeViewElement( "head" );
		} );
		assertElementOf( builder ).isEqualTo( new NodeViewElement( "head" ) );
	}

	private NodeViewElementAssert assertElementOf( ViewElementBuilder<NodeViewElement> builder ) {
		return new NodeViewElementAssert( builder.build( threadLocal ) );
	}

	@RequiredArgsConstructor
	private class NodeViewElementAssert
	{
		private final AbstractNodeViewElement actual;

		NodeViewElementAssert isEqualTo( AbstractNodeViewElement expected ) {
			assertThat( actual.getTagName() ).isEqualTo( expected.getTagName() );
			assertThat( actual.getAttributes() ).isEqualTo( expected.getAttributes() );
			return this;
		}
	}

	@Test
	void customBuilderContextIsCreatedIfNoGlobalAvailable() {
		AtomicReference<ViewElementBuilderContext> holder = new AtomicReference<>();
		ViewElementBuilder<?> builder = builderContext -> {
			if ( holder.get() != null ) {
				assertThat( builderContext ).isNotSameAs( holder.get() );
				return new NodeViewElement( "div" );
			}
			holder.set( builderContext );
			return null;
		};

		assertThat( builder.build() ).isNull();
		assertThat( builder.build() ).isNotNull();
	}

	@Test
	void requestBoundGlobalContextIsUsedIfNoThreadLocal() {
		bindBuilderContextToRequest();

		ViewElementBuilder<?> target = mock( ViewElementBuilder.class );
		ViewElementBuilder<?> builder = target::build;

		builder.build();

		verify( target ).build( requestBound );
		verify( target, never() ).build( threadLocal );
	}

	@Test
	void threadLocalTakesPrecedenceOverRequestBound() {
		bindBuilderContextToRequest();
		ViewElementBuilderContextHolder.setViewElementBuilderContext( threadLocal );

		ViewElementBuilder<?> target = mock( ViewElementBuilder.class );
		ViewElementBuilder<?> builder = target::build;

		builder.build();

		verify( target ).build( threadLocal );
		verify( target, never() ).build( requestBound );
	}

	private void bindBuilderContextToRequest() {
		assertEquals( Optional.empty(), WebResourceUtils.currentViewElementBuilderContext() );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.VIEW_ELEMENT_BUILDER_CONTEXT_KEY,
		                               RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( requestBound );
		RequestContextHolder.setRequestAttributes( attributes );
		assertEquals( Optional.of( requestBound ), WebResourceUtils.currentViewElementBuilderContext() );
	}

	@Test
	void andThenDoesNothingIfNullPostProcessorPassed() {
		ViewElementBuilder<?> builder = builderContext -> new NodeViewElement( "div" );
		assertThat( builder.andThen( null ) ).isSameAs( builder );
	}

	@Test
	void andThenReturnsNewBuilderWithSettingsApplied() {
		ViewElementBuilder<NodeViewElement> builder = builderContext -> new NodeViewElement( "div" );
		ViewElementBuilder<NodeViewElement> composed = builder.andThen( ( ( builderContext, element ) -> element.setAttribute( "test", "hello" ) ) );
		assertThat( composed ).isNotSameAs( builder );

		NodeViewElement node = composed.build( new DefaultViewElementBuilderContext() );
		assertThat( node.getTagName() ).isEqualTo( "div" );
		assertThat( node.getAttribute( "test" ) ).isEqualTo( "hello" );
	}

	@Test
	void mapReturnsNewBuilderOfNewType() {
		NodeViewElementBuilder node = new NodeViewElementBuilder( "mytag" );

		ViewElementBuilder<TextViewElement> tagName = node.map( n -> TextViewElement.text( n.getTagName() ) );
		assertThat( tagName ).isNotNull();
		assertThat( tagName.build().getText() ).isEqualTo( "mytag" );

		ViewElementBuilder<ContainerViewElement> container = tagName
				.map( ( builderContext, textViewElement ) -> new ContainerViewElementBuilder().add( textViewElement ).build( builderContext ) )
				.andThen( ( builderContext, transformed ) -> transformed.addChild( TextViewElement.text( "second" ) ) );

		ContainerViewElement built = container.build();
		assertThat( built ).isNotNull();
		assertThat( built.getChildren() ).hasSize( 2 );
		assertThat( ( (TextViewElement) built.getChildren().get( 0 ) ).getText() ).isEqualTo( "mytag" );
		assertThat( ( (TextViewElement) built.getChildren().get( 1 ) ).getText() ).isEqualTo( "second" );
	}
}
