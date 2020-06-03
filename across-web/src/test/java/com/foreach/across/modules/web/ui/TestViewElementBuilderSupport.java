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
package com.foreach.across.modules.web.ui;

import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class TestViewElementBuilderSupport
{
	static class Builder extends ViewElementBuilderSupport<MutableViewElement, Builder>
	{
		@Override
		protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
			return mock( MutableViewElement.class );
		}

		@Override
		protected void registerWebResources( WebResourceRegistry webResourceRegistry ) {
			webResourceRegistry.add( "item", "value" );
		}
	}

	@Test
	void customSupplierIsUsed( @Mock ViewElementBuilderContext builderContext ) {
		when( builderContext.getMessage( "say.hello" ) ).thenReturn( "hello!" );

		Function<ViewElementBuilderContext, TextViewElement> function = bc -> new TextViewElement( bc.getMessage( "say.hello" ) );
		Supplier<TextViewElement> supplier = () -> new TextViewElement( "goodbye" );

		MutableViewElement text = new Builder().elementSupplier( supplier ).build( builderContext );
		assertNotNull( text );
		assertEquals( "goodbye", ( (TextViewElement) text ).getText() );

		text = new Builder().elementSupplier( function ).build( builderContext );
		assertNotNull( text );
		assertEquals( "hello!", ( (TextViewElement) text ).getText() );
	}

	@Test
	void postProcessorsAreExecuted() {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		ViewElement element = new Builder().postProcessor( one ).postProcessor( two ).build( builderContext );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

	@Test
	void defaultPostProcessorsAreExecuted() {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		DefaultViewElementPostProcessor.add( builderContext, two );

		ViewElement element = new Builder().postProcessor( one ).build( builderContext );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

	@Test
	void manuallyRegisteringADefaultPostProcessor() {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		DefaultViewElementPostProcessor.add( builderContext, two );

		ViewElement element = new Builder()
				.postProcessor( one )
				.postProcessor( DefaultViewElementPostProcessor.INSTANCE )
				.build( builderContext );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

	@Test
	void webResourcesAreRegisteredIfRegistryPresent() {
		WebResourceRegistry registry = mock( WebResourceRegistry.class );
		ViewElementBuilderContext builderContext = mock( ViewElementBuilderContext.class );
		when( builderContext.getAttribute( WebResourceRegistry.class ) ).thenReturn( registry );

		ViewElement element = new Builder().build( builderContext );

		assertNotNull( element );
		verify( registry ).add( "item", "value" );
	}
}
