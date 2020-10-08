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

import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.test.support.AbstractViewElementTemplateTest;
import org.junit.jupiter.api.Test;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class TestMetaWebResourceRendering extends AbstractViewElementTemplateTest
{
	@Test
	public void noMetaName() {
		renderAndExpect( WebResource.meta().refresh( "30; http://www.google.be" ),
		                 "<meta http-equiv=\"refresh\" content=\"30; http://www.google.be\" />" );
	}

	@Test
	public void defaultMetaNameIsViewElementName() {
		renderAndExpect(
				WebResource.meta().name( "meta" ).content( "value" ),
				"<meta name=\"meta\" content=\"value\"/>" );
	}

	@Test
	public void customMetaName() {
		renderAndExpect(
				WebResource.meta( "key" ).name( "meta" ).content( "value" ).httpEquiv( "refresh" ),
				"<meta name=\"key\" http-equiv=\"refresh\" content=\"value\"/>" );
	}

	@Test
	public void withCustomAttribute() {
		renderAndExpect(
				WebResource.meta( "hello" ).content( "there" ).attribute( "data-meta-set", 1 ),
				"<meta name='hello' content='there' data-meta-set='1' />"
		);
	}
}
