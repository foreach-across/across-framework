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

import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.ViewElementGeneratorBuilder;

public interface ViewElementBuilderFactory
{
	ContainerViewElementBuilder container();

	<ITEM, VIEW_ELEMENT extends ViewElement> ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> generator(
			Class<ITEM> itemClass, Class<VIEW_ELEMENT> viewElementClass
	);

	TextViewElementBuilder text();

	TextViewElementBuilder text( String text );

	TextViewElementBuilder html();

	TextViewElementBuilder html( String html );

	NodeViewElementBuilder node( String tagName );
}
