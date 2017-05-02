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
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IdentifierSequences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHtmlIdStore
{
	@Mock
	private ITemplateContext templateContext;

	@Mock
	private HtmlViewElement one, two;

	private HtmlIdStore htmlIdStore;
	private IdentifierSequences identifierSequences;

	@Before
	public void before() {
		htmlIdStore = new HtmlIdStore();
		identifierSequences = new IdentifierSequences();
		when( templateContext.getIdentifierSequences() ).thenReturn( identifierSequences );

		when( one.getHtmlId() ).thenReturn( "one" );
		when( two.getHtmlId() ).thenReturn( "two" );
	}

	@Test
	public void noIdSpecified() {
		assertNull( htmlIdStore.retrieveHtmlId( templateContext, mock( ViewElement.class ) ) );
		assertNull( htmlIdStore.retrieveHtmlId( templateContext, mock( HtmlViewElement.class ) ) );
	}

	@Test
	public void sameIdReturnedForSameInstance() {
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}

	@Test
	public void differentInstanceIncrementsId() {
		when( two.getHtmlId() ).thenReturn( "one" );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
		assertEquals( "one1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}

	@Test
	public void increaseAndDecreaseLevel() {
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );

		htmlIdStore.increaseLevel();
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );

		htmlIdStore.decreaseLevel();
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "two1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}
}
