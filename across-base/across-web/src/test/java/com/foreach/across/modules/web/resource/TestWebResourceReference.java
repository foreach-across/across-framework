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
package com.foreach.across.modules.web.resource;

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestWebResourceReference
{
	@Test
	public void viewElementBuilderIsRequired() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> WebResourceReference.builder().key( "someKey" ).build() );
	}

	@Test
	public void defaultOrder() {
		WebResourceReference reference = WebResourceReference.builder().viewElementBuilder( mock( ViewElementBuilder.class ) ).build();
		assertThat( reference ).isNotNull();
		assertThat( reference.getKey() ).isNull();
		assertThat( reference.getOrder() ).isEqualTo( WebResourceReference.DEFAULT_ORDER );
	}

	@Test
	public void defaultKeyFromKeyProvider() {
		WebResourceKeyProvider viewElementBuilder = mock( WebResourceKeyProvider.class, withSettings().extraInterfaces( ViewElementBuilder.class ) );
		when( viewElementBuilder.getWebResourceKey() ).thenReturn( Optional.of( "defaultKey" ) );

		WebResourceReference reference = WebResourceReference.builder().viewElementBuilder( (ViewElementBuilder) viewElementBuilder ).build();
		assertThat( reference.getKey() ).isEqualTo( "defaultKey" );
	}

	@Test
	public void customKeyTakesPrecedence() {
		WebResourceKeyProvider viewElementBuilder = mock( WebResourceKeyProvider.class, withSettings().extraInterfaces( ViewElementBuilder.class ) );
		when( viewElementBuilder.getWebResourceKey() ).thenReturn( Optional.of( "defaultKey" ) );

		WebResourceReference reference = WebResourceReference.builder()
		                                                     .key( "customKey" ).viewElementBuilder( (ViewElementBuilder) viewElementBuilder )
		                                                     .build();
		assertThat( reference.getKey() ).isEqualTo( "customKey" );
	}
}
