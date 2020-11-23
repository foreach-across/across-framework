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

import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.VoidNodeViewElement;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestVoidNodeViewElementBuilder extends AbstractViewElementBuilderTest<VoidNodeViewElementBuilder, VoidNodeViewElement>
{
	@Override
	protected VoidNodeViewElementBuilder createBuilder( ViewElementBuilderFactory factory ) {
		return new VoidNodeViewElementBuilder( "hr" );
	}

	@Test
	public void setAttributes() {
		builder.tagName( "a" ).attribute( "href", "somelink" ).removeAttribute( "class" ).data( "role", "link" );

		build();

		assertEquals( "a", element.getTagName() );
		assertEquals( "somelink", element.getAttribute( "href" ) );
		assertEquals( "link", element.getAttribute( "data-role" ) );
		assertNull( element.getAttribute( "class" ) );
		assertTrue( element.hasAttribute( "class" ) );
	}
}
