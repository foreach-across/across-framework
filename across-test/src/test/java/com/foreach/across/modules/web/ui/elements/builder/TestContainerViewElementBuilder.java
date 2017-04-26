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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.Optional;

import static com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils.find;
import static org.junit.Assert.*;

public class TestContainerViewElementBuilder extends AbstractViewElementBuilderTest<ContainerViewElementBuilder, ContainerViewElement>
{
	@Override
	protected ContainerViewElementBuilder createBuilder( ViewElementBuilderFactory factory ) {
		return new ContainerViewElementBuilder();
	}

	@Test
	public void defaults() {
		build();

		assertFalse( element.hasChildren() );
	}

	@Test
	public void addElements() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );

		builder.add( textOne ).add( textTwo );

		build();
		assertEquals( 2, element.getChildren().size() );
		assertEquals( Optional.of( textOne ), find( element, "textOne" ) );
		assertEquals( Optional.of( textTwo ), find( element, "textTwo" ) );
		assertSame( textOne, element.getChildren().get( 0 ) );

		builder.sort( "textTwo", "textOne" );

		build();
		assertEquals( 2, element.getChildren().size() );
		assertEquals( Optional.of( textOne ), find( element, "textOne" ) );
		assertEquals( Optional.of( textTwo ), find( element, "textTwo" ) );
		assertSame( textTwo, element.getChildren().get( 0 ) );
	}

	@Test
	public void addFirstElements() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );

		builder.addFirst( textOne ).addFirst( textTwo );

		build();
		assertEquals( 2, element.getChildren().size() );
		assertEquals( Optional.of( textOne ), find( element, "textOne" ) );
		assertEquals( Optional.of( textTwo ), find( element, "textTwo" ) );
		assertSame( textTwo, element.getChildren().get( 0 ) );
	}

	@Test
	public void addElementBuilder() {
		TextViewElementBuilder textBuilder = new TextViewElementBuilder().name( "textThree" ).text( "text three" );

		builder.add( textBuilder );

		build();
		assertEquals( 1, element.getChildren().size() );
		assertSame( "text three", find( element, "textThree", TextViewElement.class )
				.orElse( new TextViewElement() ).getText() );
	}

	@Test
	public void addMix() {
		TextViewElement textOne = new TextViewElement( "textOne", "text 1" );
		TextViewElement textTwo = new TextViewElement( "textTwo", "text 2" );
		TextViewElementBuilder textFour = new TextViewElementBuilder().name( "textThree" ).text( "text 3" );
		TextViewElementBuilder textThree = new TextViewElementBuilder().name( "textFour" ).text( "text 4" );

		builder.sort( "textFour", "textTwo", "textThree", "textOne" )
		       .add( textThree ).add( textOne, textTwo ).addFirst( textFour );

		build();
		assertEquals( 4, element.getChildren().size() );

		Iterator<ViewElement> iterator = element.getChildren().iterator();
		assertEquals( "textFour", iterator.next().getName() );
		assertEquals( "textTwo", iterator.next().getName() );
		assertEquals( "textThree", iterator.next().getName() );
		assertEquals( "textOne", iterator.next().getName() );
	}
}
