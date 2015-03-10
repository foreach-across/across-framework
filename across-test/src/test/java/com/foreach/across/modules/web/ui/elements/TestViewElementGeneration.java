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

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Tests the generation of view elements based on content lists.
 *
 * @author Arne Vandamme
 */
public class TestViewElementGeneration extends AbstractViewElementTemplateTest
{
	private final Collection<Person> people = Arrays.asList(
			new Person( "John Doe", "john@doe.com" ),
			new Person( "Jane Doe", "jane@doe.com" )
	);

	@Test
	public void manualTableViewElement() {
		NodeViewElement table = NodeViewElement.forTag( "table" );

		for ( Person person : people ) {
			NodeViewElement row = NodeViewElement.forTag( "tr" );

			NodeViewElement name = NodeViewElement.forTag( "td" );
			name.add( new TextViewElement( person.getName() ) );
			row.add( name );

			NodeViewElement email = NodeViewElement.forTag( "td" );
			email.add( new TextViewElement( person.getEmail() ) );
			row.add( email );

			table.add( row );
		}

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td>John Doe</td><td>john@doe.com</td></tr>" +
						"<tr><td>Jane Doe</td><td>jane@doe.com</td></tr>" +
						"</table>"
		);
	}

	@Test
	public void templatedTableViewElement() {
		NodeViewElement rowTemplate = NodeViewElement.forTag( "tr" );
		NodeViewElement name = NodeViewElement.forTag( "td" );
		name.add( new TextViewElement( "name", "" ) );
		rowTemplate.add( name );
		NodeViewElement email = NodeViewElement.forTag( "td" );
		email.add( new TextViewElement( "email", "" ) );
		rowTemplate.add( email );

		NodeViewElement table = NodeViewElement.forTag( "table" );

		ViewElementGenerator<Person, NodeViewElement> generator = new ViewElementGenerator<>();
		generator.setItemTemplate( rowTemplate );
		generator.setItems( people );
		generator.setCreationCallback(
				new ViewElementGenerator.CreationCallback<Person, NodeViewElement>()
				{
					@Override
					public NodeViewElement create( Person item, NodeViewElement template ) {
						template.<TextViewElement>get( "name" ).setText( item.getName() );
						template.<TextViewElement>get( "email" ).setText( item.getEmail() );

						return template;
					}
				}
		);

		table.add( generator );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td>John Doe</td><td>john@doe.com</td></tr>" +
						"<tr><td>Jane Doe</td><td>jane@doe.com</td></tr>" +
						"</table>"
		);
	}

	@Test
	public void templatedBuilderTableViewElement() {

		ViewElementBuilder<NodeViewElement> builder = new ViewElementBuilder<NodeViewElement>()
		{
			@Override
			public NodeViewElement build( ViewElementBuilderContext builderContext ) {
				NodeViewElement rowTemplate = NodeViewElement.forTag( "tr" );
				NodeViewElement name = NodeViewElement.forTag( "td" );
				name.add( new TextViewElement( "name", "" ) );
				rowTemplate.add( name );
				NodeViewElement email = NodeViewElement.forTag( "td" );
				email.add( new TextViewElement( "email", "" ) );
				rowTemplate.add( email );

				return rowTemplate;
			}
		};

		NodeViewElement table = NodeViewElement.forTag( "table" );

		ViewElementGenerator<Person, NodeViewElement> generator = new ViewElementGenerator<>();
		generator.setItemTemplate( builder );
		generator.setItems( people );
		generator.setCreationCallback(
				new ViewElementGenerator.CreationCallback<Person, NodeViewElement>()
				{
					@Override
					public NodeViewElement create( Person item, NodeViewElement template ) {
						template.<TextViewElement>get( "name" ).setText( item.getName() );
						template.<TextViewElement>get( "email" ).setText( item.getEmail() );

						return template;
					}
				}
		);

		table.add( generator );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td>John Doe</td><td>john@doe.com</td></tr>" +
						"<tr><td>Jane Doe</td><td>jane@doe.com</td></tr>" +
						"</table>"
		);
	}

	private static class Person
	{
		private String name, email;

		public Person( String name, String email ) {
			this.name = name;
			this.email = email;
		}

		public String getName() {
			return name;
		}

		public String getEmail() {
			return email;
		}
	}
}
