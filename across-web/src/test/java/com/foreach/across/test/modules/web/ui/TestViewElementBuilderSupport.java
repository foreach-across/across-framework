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

import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.ui.*;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings("unchecked")
public class TestViewElementBuilderSupport
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
	public void postProcessorsAreExecuted() {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		ViewElement element = new Builder().postProcessor( one ).postProcessor( two ).build( builderContext );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

	@Test
	public void defaultPostProcessorsAreExecuted() {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		DefaultViewElementPostProcessor.add( builderContext, two );

		ViewElement element = new Builder().postProcessor( one ).build( builderContext );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

	@Test
	public void manuallyRegisteringADefaultPostProcessor() {
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
	public void webResourcesAreRegisteredIfRegistryPresent() {
		WebResourceRegistry registry = mock( WebResourceRegistry.class );

		RequestAttributes attributes = mock( RequestAttributes.class );
		when( attributes.getAttribute( WebResourceUtils.REGISTRY_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST ) )
				.thenReturn( registry );
		RequestContextHolder.setRequestAttributes( attributes );

		try {
			ViewElement element = new Builder().build( new DefaultViewElementBuilderContext() );

			assertNotNull( element );
			verify( registry ).add( "item", "value" );
		}
		finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}
}
