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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElementSupport;

/**
 * A simple text item.
 */
public class TextViewElement extends ViewElementSupport implements ConfigurableTextViewElement
{
	public static final String ELEMENT_TYPE = StandardViewElements.TEXT;
	boolean escapeXml = true;
	private String text;

	public TextViewElement() {
		this( null, true );
	}

	public TextViewElement( String text ) {
		this( text, true );
	}

	public TextViewElement( String name, String text ) {
		this( text, true );
		setName( name );
	}

	public TextViewElement( String text, boolean escapeXml ) {
		super( ELEMENT_TYPE );
		this.text = text;
		this.escapeXml = escapeXml;
	}

	public TextViewElement( String name, String text, boolean escapeXml ) {
		super( ELEMENT_TYPE );
		this.text = text;
		this.escapeXml = escapeXml;
		setName( name );
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText( String text ) {
		this.text = text;
	}

	public boolean isEscapeXml() {
		return escapeXml;
	}

	public void setEscapeXml( boolean escapeXml ) {
		this.escapeXml = escapeXml;
	}
}


