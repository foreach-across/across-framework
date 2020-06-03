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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderFactory;
import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;
import com.foreach.across.test.support.AbstractViewElementBuilderTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings( "unchecked" )
public class TestViewElementGeneratorBuilder
		extends AbstractViewElementBuilderTest<ViewElementGeneratorBuilder<Object, ViewElement>, ViewElementGenerator<Object, ViewElement>>
{
	@Override
	protected ViewElementGeneratorBuilder<Object, ViewElement> createBuilder( ViewElementBuilderFactory builderFactory ) {
		return new ViewElementGeneratorBuilder<>();
	}

	@Test
	public void itemsAndCallback() {
		Collection items = Arrays.asList( "one", "two" );
		ViewElementGenerator.CreationCallback callback = mock( ViewElementGenerator.CreationCallback.class );
		ViewElementBuilder itemBuilder = mock( ViewElementBuilder.class );

		builder.items( items ).creationCallback( callback ).itemBuilder( itemBuilder );

		build();

		assertSame( builderContext, element.getItemBuilderContext() );
		assertSame( callback, element.getCreationCallback() );
		assertSame( itemBuilder, element.getItemTemplateAsBuilder() );
		assertEquals( 2, element.size() );
	}
}
