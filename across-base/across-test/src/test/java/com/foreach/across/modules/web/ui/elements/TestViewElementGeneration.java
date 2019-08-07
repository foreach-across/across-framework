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
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils.find;

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
		NodeViewElement table = new NodeViewElement( "table" );

		for ( Person person : people ) {
			NodeViewElement row = new NodeViewElement( "tr" );

			NodeViewElement name = new NodeViewElement( "td" );
			name.setHtmlId( "name-cell" );
			name.addChild( new TextViewElement( person.getName() ) );
			row.addChild( name );

			NodeViewElement email = new NodeViewElement( "td" );
			email.setHtmlId( "email-cell" );
			email.addChild( new TextViewElement( person.getEmail() ) );
			row.addChild( email );

			table.addChild( row );
		}

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td id='name-cell'>John Doe</td><td id='email-cell'>john@doe.com</td></tr>" +
						"<tr><td id='name-cell1'>Jane Doe</td><td id='email-cell1'>jane@doe.com</td></tr>" +
						"</table>"
		);
	}

	@Test
	public void viewElementTemplate() {
		NodeViewElement rowTemplate = new NodeViewElement( "tr" );
		NodeViewElement name = new NodeViewElement( "td" );
		name.setHtmlId( "name-cell" );
		name.addChild( new TextViewElement( "name", "" ) );
		rowTemplate.addChild( name );
		NodeViewElement email = new NodeViewElement( "td" );
		email.setHtmlId( "email-cell" );
		email.addChild( new TextViewElement( "email", "" ) );
		rowTemplate.addChild( email );

		NodeViewElement table = new NodeViewElement( "table" );

		ViewElementGenerator<Person, NodeViewElement> generator = new ViewElementGenerator<>();
		generator.setItemTemplate( rowTemplate );
		generator.setItems( people );
		generator.setCreationCallback(
				( itemStats, template ) -> {
					Person item = itemStats.getItem();
					find( template, "name", TextViewElement.class )
							.ifPresent( t -> t.setText( item.getName() ) );
					find( template, "email", TextViewElement.class )
							.ifPresent( t -> t.setText( item.getEmail() ) );

					return template;
				}
		);

		table.addChild( generator );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td id='name-cell'>John Doe</td><td id='email-cell'>john@doe.com</td></tr>" +
						"<tr><td id='name-cell1'>Jane Doe</td><td id='email-cell1'>jane@doe.com</td></tr>" +
						"</table>"
		);

		email.setCustomTemplate( CUSTOM_TEMPLATE );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td id='name-cell'>John Doe</td>" + CUSTOM_TEMPLATE_OUTPUT + "</tr>" +
						"<tr><td id='name-cell1'>Jane Doe</td>" + CUSTOM_TEMPLATE_OUTPUT + "</tr>" +
						"</table>"
		);

		renderAndExpect( generator,
		                 "<tr><td id='name-cell'>John Doe</td>" + CUSTOM_TEMPLATE_OUTPUT + "</tr>" +
				                 "<tr><td id='name-cell1'>Jane Doe</td>" + CUSTOM_TEMPLATE_OUTPUT + "</tr>"
		);
	}

	@Test
	public void viewElementBuilderTemplate() {
		ViewElementBuilder<NodeViewElement> builder = builderContext -> {
			NodeViewElement rowTemplate = new NodeViewElement( "tr" );
			NodeViewElement name = new NodeViewElement( "td" );
			name.setHtmlId( "name-cell" );
			name.addChild( new TextViewElement( "name", "" ) );
			rowTemplate.addChild( name );
			NodeViewElement email = new NodeViewElement( "td" );
			email.setHtmlId( "email-cell" );
			email.addChild( new TextViewElement( "email", "" ) );
			rowTemplate.addChild( email );

			return rowTemplate;
		};

		NodeViewElement table = new NodeViewElement( "table" );

		ViewElementGenerator<Person, NodeViewElement> generator = new ViewElementGenerator<>();
		generator.setItemTemplate( builder );
		generator.setItems( people );
		generator.setCreationCallback(
				( itemStats, template ) -> {
					find( template, "name", TextViewElement.class )
							.ifPresent( t -> t.setText( itemStats.getItem().getName() ) );
					find( template, "email", TextViewElement.class )
							.ifPresent( t -> t.setText( itemStats.getItem().getEmail() ) );

					return template;
				}
		);

		table.addChild( generator );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td id='name-cell'>John Doe</td><td id='email-cell'>john@doe.com</td></tr>" +
						"<tr><td id='name-cell1'>Jane Doe</td><td id='email-cell1'>jane@doe.com</td></tr>" +
						"</table>"
		);
	}

	@Test
	public void callbackOnly() {
		NodeViewElement table = new NodeViewElement( "table" );

		ViewElementGenerator<Person, NodeViewElement> generator = new ViewElementGenerator<>();
		generator.setItems( people );
		generator.setCreationCallback( ( itemStats, template ) -> {
			Person person = itemStats.getItem();
			NodeViewElement row = new NodeViewElement( "tr" );

			NodeViewElement name = new NodeViewElement( "td" );
			name.setHtmlId( "name-cell" );
			name.addChild( new TextViewElement( person.getName() ) );
			row.addChild( name );

			NodeViewElement email = new NodeViewElement( "td" );
			email.setHtmlId( "email-cell" );
			email.addChild( new TextViewElement( person.getEmail() ) );
			row.addChild( email );

			return row;
		} );

		table.addChild( generator );

		renderAndExpect(
				table,
				"<table>" +
						"<tr><td id='name-cell'>John Doe</td><td id='email-cell'>john@doe.com</td></tr>" +
						"<tr><td id='name-cell1'>Jane Doe</td><td id='email-cell1'>jane@doe.com</td></tr>" +
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
