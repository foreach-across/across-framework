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
package com.foreach.across.test.modules.web.ui;

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContextHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TestViewElementBuilderContextHolder
{
	private ViewElementBuilderContext renderContext = mock( ViewElementBuilderContext.class );

	@After
	@Before
	public void clean() {
		ViewElementBuilderContextHolder.clearViewElementBuilderContext();
	}

	@Test
	public void noContextAssociated() {
		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}

	@Test
	public void singleContextAssociated() {
		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.setViewElementBuilderContext( renderContext ) );
		assertEquals( Optional.of( renderContext ), ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}

	@Test
	public void associatingReplacesPreviousContext() {
		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.setViewElementBuilderContext( renderContext ) );

		ViewElementBuilderContext otherRenderContext = mock( ViewElementBuilderContext.class );
		assertEquals( Optional.of( renderContext ),
		              ViewElementBuilderContextHolder.setViewElementBuilderContext( otherRenderContext ) );

		assertEquals( Optional.of( otherRenderContext ),
		              ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}

	@Test
	public void clearRemovesTheAssociatedContext() {
		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.setViewElementBuilderContext( renderContext ) );
		assertEquals( Optional.of( renderContext ), ViewElementBuilderContextHolder.clearViewElementBuilderContext() );

		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}

	@Test
	public void associatingNullContextIsSameAsClear() {
		ViewElementBuilderContextHolder.setViewElementBuilderContext( renderContext );
		assertEquals( Optional.of( renderContext ), ViewElementBuilderContextHolder
				.setViewElementBuilderContext( (ViewElementBuilderContext) null ) );

		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}

	@Test
	public void associatingEmptyOptionalContextIsSameAsClear() {
		ViewElementBuilderContextHolder.setViewElementBuilderContext( renderContext );
		assertEquals( Optional.of( renderContext ),
		              ViewElementBuilderContextHolder.setViewElementBuilderContext( Optional.empty() ) );

		assertEquals( Optional.empty(), ViewElementBuilderContextHolder.getViewElementBuilderContext() );
	}
}
