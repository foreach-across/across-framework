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

public class TestContainerViewElement extends AbstractViewElementTemplateTest
{
	@Test
	public void multipleTextElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.add( new TextViewElement( "one, " ) );
		container.add( new TextViewElement( "two, " ) );
		container.add( new TextViewElement( "three" ) );

		renderAndExpect( container, "one, two, three" );
	}

	@Test
	public void nestedContainerElements() {
		ContainerViewElement container = new ContainerViewElement();
		container.add( new TextViewElement( "one, " ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.add( new TextViewElement( "two, " ) );
		subContainer.add( new TextViewElement( "three" ) );

		container.add( subContainer );
		renderAndExpect( container, "one, two, three" );
	}

	@Test
	public void customTemplateWithoutNesting() {
		ContainerViewElement container = new ContainerViewElement();
		container.setCustomTemplate( "th/test/elements/container" );
		container.add( new TextViewElement( "one" ) );
		container.add( new TextViewElement( "two" ) );
		container.add( new TextViewElement( "three" ) );

		renderAndExpect( container, "<ul><li>one</li><li>two</li><li>three</li></ul>" );
	}

	@Test
	public void customTemplateWithNesting() {
		ContainerViewElement container = new ContainerViewElement();
		container.setCustomTemplate( "th/test/elements/container :: nested(${component})" );
		container.add( new TextViewElement( "one" ) );

		ContainerViewElement subContainer = new ContainerViewElement();
		subContainer.add( new TextViewElement( "two, " ) );
		subContainer.add( new TextViewElement( "three" ) );

		ContainerViewElement otherSubContainer = new ContainerViewElement();
		otherSubContainer.setCustomTemplate( "th/test/elements/container" );
		otherSubContainer.add( new TextViewElement( "four" ) );
		otherSubContainer.add( new TextViewElement( "five" ) );

		subContainer.add( otherSubContainer );

		container.add( subContainer );

		renderAndExpect( container,
		                 "<ol><li>one</li><li>two, three<ul><li>four</li><li>five</li></ul></li></ol>" );
	}

}
