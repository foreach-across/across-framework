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
package com.foreach.across.modules.web.ui;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings("unchecked")
public class TestDefaultViewElementPostProcessor
{
	@Test
	public void noPostProcessorsRegistered() {
		ViewElement element = mock( ViewElement.class );
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

		DefaultViewElementPostProcessor.INSTANCE.postProcess( builderContext, element );
	}

	@Test
	public void singlePostProcessor() {
		ViewElement element = mock( ViewElement.class );
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		DefaultViewElementPostProcessor.add( builderContext, one );
		DefaultViewElementPostProcessor.add( builderContext, two );
		DefaultViewElementPostProcessor.remove( builderContext, one );

		DefaultViewElementPostProcessor.INSTANCE.postProcess( builderContext, element );

		verify( two ).postProcess( builderContext, element );
		verifyNoInteractions( one );
	}

	@Test
	public void postProcessorsAreAttachedToBuilderContext() {
		ViewElement element = mock( ViewElement.class );

		ViewElementBuilderContext builderContextOne = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		DefaultViewElementPostProcessor.add( builderContextOne, one );

		ViewElementBuilderContext builderContextTwo = new DefaultViewElementBuilderContext();
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );
		DefaultViewElementPostProcessor.add( builderContextTwo, two );

		DefaultViewElementPostProcessor.INSTANCE.postProcess( builderContextOne, element );

		verify( one ).postProcess( builderContextOne, element );
		verifyNoInteractions( two );

		DefaultViewElementPostProcessor.INSTANCE.postProcess( builderContextTwo, element );
		verify( one ).postProcess( builderContextOne, element );
		verify( two ).postProcess( builderContextTwo, element );
	}

	@Test
	public void multiplePostProcessors() {
		ViewElement element = mock( ViewElement.class );
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext();

		ViewElementPostProcessor one = mock( ViewElementPostProcessor.class );
		ViewElementPostProcessor two = mock( ViewElementPostProcessor.class );

		DefaultViewElementPostProcessor.add( builderContext, one );
		DefaultViewElementPostProcessor.add( builderContext, two );

		DefaultViewElementPostProcessor.INSTANCE.postProcess( builderContext, element );

		verify( one ).postProcess( builderContext, element );
		verify( two ).postProcess( builderContext, element );
	}

}
