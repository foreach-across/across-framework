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
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IdentifierSequences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TestHtmlIdStore
{
	@Mock
	private ITemplateContext templateContext;

	@Mock
	private HtmlViewElement one, two;

	private HtmlIdStore htmlIdStore;

	@BeforeEach
	void before() {
		htmlIdStore = new HtmlIdStore();
		IdentifierSequences identifierSequences = new IdentifierSequences();
		Mockito.lenient().when( templateContext.getIdentifierSequences() ).thenReturn( identifierSequences );

		Mockito.lenient().when( one.getHtmlId() ).thenReturn( "one" );
		Mockito.lenient().when( two.getHtmlId() ).thenReturn( "two" );
	}

	@Test
	void noIdSpecified() {
		assertNull( htmlIdStore.retrieveHtmlId( templateContext, mock( ViewElement.class ) ) );
		assertNull( htmlIdStore.retrieveHtmlId( templateContext, mock( HtmlViewElement.class ) ) );
	}

	@Test
	void sameIdReturnedForSameInstance() {
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}

	@Test
	void differentInstanceIncrementsId() {
		when( two.getHtmlId() ).thenReturn( "one" );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "one1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
		assertEquals( "one1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}

	@Test
	void increaseAndDecreaseLevel() {
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );

		htmlIdStore.increaseLevel();
		assertEquals( "two", htmlIdStore.retrieveHtmlId( templateContext, two ) );

		htmlIdStore.decreaseLevel();
		assertEquals( "one", htmlIdStore.retrieveHtmlId( templateContext, one ) );
		assertEquals( "two1", htmlIdStore.retrieveHtmlId( templateContext, two ) );
	}
}
