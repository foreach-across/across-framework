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
package com.foreach.across.modules.web.ui.elements.support;

import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestContainerLayoutViewElementPostProcessor
{
	private ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

	@Test
	public void simpleMove() {
		container( n( "left" ), n( "content" ) )
				.apply( layout -> layout.move( "content" ).to( "left" ) )
				.expect( c( "left", n( "content" ) ) );

		container( n( "left" ), n( "a" ), n( "b", n( "c" ) ) )
				.apply( layout -> layout.move( "c", "b", "a" ).to( "left" ) )
				.expect( c( "left", n( "c" ), n( "b" ), n( "a" ) ) );
	}

	@Test
	public void simpleRemove() {
		container( n( "left" ), n( "content", n( "child" ) ) )
				.apply( layout -> layout.remove( "left" ) )
				.expect( n( "content", n( "child" ) ) )
				.apply( layout -> layout.remove( "child", "content" ) )
				.expect();
	}

	@Test
	public void simpleAdd() {
		container( n( "left" ) )
				.apply(
						layout -> layout.add(
								new ContainerViewElementBuilder()
										.name( "body" )
										.add( new NodeViewElementBuilder( "h1" ).name( "h1" ) )
						)
				)
				.expect( n( "left" ), c( "body", n( "h1" ) ) );
	}

	@Test
	public void addNewElementWithExistingElementMember() {
		container( n( "left" ), n( "text" ) )
				.apply(
						layout -> layout.add(
								new ContainerViewElementBuilder()
										.name( "body" )
										.add( layout.remove( "text" ) )
						)
				)
				.expect( n( "left" ), c( "body", n( "text" ) ) );
	}

	private ContainerLayoutTester container( ViewElement... children ) {
		return new ContainerLayoutTester( c( "root", children ) );
	}

	@RequiredArgsConstructor
	private class ContainerLayoutTester
	{
		private final ContainerViewElement container;

		private ContainerLayoutTester apply( Consumer<ContainerLayoutViewElementPostProcessor<ContainerViewElement>> consumer ) {
			ContainerLayoutViewElementPostProcessor<ContainerViewElement> layout = new ContainerLayoutViewElementPostProcessor<>();
			consumer.accept( layout );
			layout.postProcess( builderContext, container );
			return this;
		}

		private ContainerLayoutTester expect( ViewElement... children ) {
			ContainerViewElement expected = c( "root", children );
			try {
				verifyContainer( container, expected );
			}
			catch ( AssertionError ae ) {
				throw new ComparisonFailure( "Container layout is different", stringOf( expected ), stringOf( container ) );
			}

			return this;
		}

		private void verifyContainer( ContainerViewElement actual, ContainerViewElement expected ) {
			assertThat( actual ).isNotNull();
			assertThat( expected ).isNotNull();
			assertThat( actual.getName() ).isEqualTo( expected.getName() );
			assertThat( actual.getChildren().size() ).isEqualTo( expected.getChildren().size() );
			for ( int i = 0; i < actual.getChildren().size(); i++ ) {
				ContainerViewElement actualChild = (ContainerViewElement) actual.getChildren().get( i );
				ContainerViewElement expectedChild = (ContainerViewElement) expected.getChildren().get( i );
				verifyContainer( actualChild, expectedChild );
			}
		}

		private String stringOf( ContainerViewElement container ) {
			StringBuilder output = new StringBuilder();
			output.append( container.getName() );
			String stringOfChildren = container.getChildren().stream().map( ContainerViewElement.class::cast )
			                                   .map( this::stringOf )
			                                   .collect( Collectors.joining( ", " ) );

			if ( !stringOfChildren.isEmpty() ) {
				output.append( "( " ).append( stringOfChildren ).append( " )" );
			}

			return output.toString();
		}
	}

	private ContainerViewElement c( String name, ViewElement... children ) {
		ContainerViewElement container = new ContainerViewElement( name );
		Arrays.stream( children ).forEach( container::addChild );
		return container;
	}

	private NodeViewElement n( String name, ViewElement... children ) {
		NodeViewElement node = new NodeViewElement( "div" );
		node.setName( name );
		Arrays.stream( children ).forEach( node::addChild );
		return node;
	}
}
