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
package com.foreach.across.modules.web.resource.elements;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestMetaWebResourceBuilder
{
	private MetaWebResourceBuilder meta = new MetaWebResourceBuilder();

	@Test
	public void defaultKeyIsNameOrHttpEquiv() {
		assertThat( meta.getWebResourceKey() ).isEmpty();
		assertThat( meta.httpEquiv( "refresh" ).getWebResourceKey() ).contains( "refresh" );
		assertThat( meta.name( "defaultName" ).getWebResourceKey() ).contains( "defaultName" );
		assertThat( meta.metaName( "specificName" ).getWebResourceKey() ).contains( "specificName" );
	}
}
