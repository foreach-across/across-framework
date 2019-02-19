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
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of {@link WebResourceReference} items to be rendered.
 *
 * @author Marc Vanbrabant
 * @see com.foreach.across.modules.web.thymeleaf.ViewElementElementProcessor
 * @since 3.1.3
 */
@RequiredArgsConstructor
public class WebResourceReferenceCollection implements Iterable<WebResourceReference>, ViewElementBuilder<ContainerViewElement>
{
	private final List<WebResourceReference> items;

	@Override
	public ContainerViewElement build( @NonNull ViewElementBuilderContext builderContext ) {
		ContainerViewElement container = new ContainerViewElement();
		items.forEach( webResourceReference -> container.addChild( webResourceReference.getViewElementBuilder().build() ) );
		return container;
	}

	@Override
	public Iterator<WebResourceReference> iterator() {
		return items.iterator();
	}
}
