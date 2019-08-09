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

import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.foreach.across.modules.web.ui.MutableViewElement.Functions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("Test default wither functions")
class TestMutableViewElement
{
	private TextViewElement text = new TextViewElement();

	@Test
	void nameWither() {
		text.set( elementName( "internal" ) );
		assertThat( text.getName() ).isEqualTo( "internal" );
	}

	@Test
	void elementTypeWither() {
		text.set( elementType( "type" ) );
		assertThat( text.getElementType() ).isEqualTo( "type" );
	}

	@Test
	void customTemplateWither() {
		text.set( customTemplate( "template" ) );
		assertThat( text.getCustomTemplate() ).isEqualTo( "template" );
	}
}
