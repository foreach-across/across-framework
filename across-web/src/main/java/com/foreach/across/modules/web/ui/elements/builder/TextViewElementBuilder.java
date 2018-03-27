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
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.TextViewElement;

public class TextViewElementBuilder extends ViewElementBuilderSupport<TextViewElement, TextViewElementBuilder>
{
	private String content;
	private Boolean escapeXml;

	public TextViewElementBuilder content( String text ) {
		content = text;
		return this;
	}

	public TextViewElementBuilder text( String text ) {
		content = text;
		escapeXml = true;
		return this;
	}

	public TextViewElementBuilder html( String html ) {
		return xml( html );
	}

	public TextViewElementBuilder xml( String xml ) {
		content = xml;
		escapeXml = false;
		return this;
	}

	public TextViewElementBuilder escapeXml( boolean escapeXml ) {
		this.escapeXml = escapeXml;
		return this;
	}

	@Override
	protected TextViewElement createElement( ViewElementBuilderContext builderContext ) {
		TextViewElement text = new TextViewElement();
		if ( content != null ) {
			text.setText( builderContext.resolveText( content ) );
		}
		if ( escapeXml != null ) {
			text.setEscapeXml( escapeXml );
		}

		return apply( text, builderContext );
	}
}
