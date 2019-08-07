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
package com.foreach.across.modules.web.resource.rules;

import com.foreach.across.modules.web.resource.WebResourceReference;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
public class TestAddWebResourceRule
{
	@Mock
	private ViewElementBuilder viewElementBuilder;

	@Mock
	private WebResourceRegistry registry;

	@Test
	public void defaultConfiguration() {
		new AddWebResourceRule().of( viewElementBuilder ).toBucket( "bucket" ).withKey( "test" ).applyTo( registry );

		verify( registry ).addResourceToBucket(
				WebResourceReference.builder().key( "test" ).viewElementBuilder( viewElementBuilder ).build(), "bucket", false
		);
	}

	@Test
	public void replaceIfPresent() {
		new AddWebResourceRule().of( viewElementBuilder ).toBucket( "bucket" ).replaceIfPresent( true ).withKey( "test" ).applyTo( registry );

		verify( registry ).addResourceToBucket(
				WebResourceReference.builder().key( "test" ).viewElementBuilder( viewElementBuilder ).build(), "bucket", true
		);
	}
}
