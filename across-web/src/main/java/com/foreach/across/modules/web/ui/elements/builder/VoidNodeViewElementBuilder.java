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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.VoidNodeViewElement;
import org.springframework.util.Assert;

public class VoidNodeViewElementBuilder extends AbstractVoidNodeViewElementBuilder<VoidNodeViewElement, VoidNodeViewElementBuilder>
{
	private String tagName;

	public VoidNodeViewElementBuilder( String tagName ) {
		tagName( tagName );
	}

	public VoidNodeViewElementBuilder tagName( String tagName ) {
		Assert.notNull( tagName );
		this.tagName = tagName;
		return this;
	}

	@Override
	protected VoidNodeViewElement createElement( ViewElementBuilderContext builderContext ) {
		VoidNodeViewElement element = new VoidNodeViewElement( tagName );

		return apply( element, builderContext );
	}
}
