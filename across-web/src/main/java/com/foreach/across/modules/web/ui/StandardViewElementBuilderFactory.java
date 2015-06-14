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

/**
 * Standard implementation of {@link com.foreach.across.modules.web.ui.ViewElementBuilderFactory} that supports
 * the most basic {@link ViewElementBuilder} types.
 */
public class StandardViewElementBuilderFactory implements ViewElementBuilderFactory
{
	@Override
	public ContainerViewElementBuilder container() {
		return new ContainerViewElementBuilder();
	}

	@Override
	public <ITEM, VIEW_ELEMENT extends ViewElement> ViewElementGeneratorBuilder<ITEM, VIEW_ELEMENT> generator(
			Class<ITEM> itemClass, Class<VIEW_ELEMENT> viewElementClass ) {
		return new ViewElementGeneratorBuilder<>();
	}

	@Override
	public TextViewElementBuilder text() {
		return new TextViewElementBuilder();
	}

	@Override
	public TextViewElementBuilder text( String text ) {
		return new TextViewElementBuilder().text( text );
	}

	@Override
	public TextViewElementBuilder html() {
		return new TextViewElementBuilder().escapeXml( false );
	}

	@Override
	public TextViewElementBuilder html( String html ) {
		return new TextViewElementBuilder().html( html );
	}

	@Override
	public NodeViewElementBuilder node() {
		return new NodeViewElementBuilder();
	}

	@Override
	public NodeViewElementBuilder node( String tagName ) {
		return new NodeViewElementBuilder().tagName( tagName );
	}
}
