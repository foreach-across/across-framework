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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementSupport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A simple text item.
 */
@Accessors(chain = true)
@Getter
@Setter
public class TextViewElement extends ViewElementSupport implements ConfigurableTextViewElement, ViewElement.WitherSetter<ViewElement>
{
	public static final String ELEMENT_TYPE = StandardViewElements.TEXT;

	private boolean escapeXml;

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
	public TextViewElement setName( String name ) {
		return (TextViewElement) super.setName( name );
	}

	@Override
	public TextViewElement setCustomTemplate( String customTemplate ) {
		return (TextViewElement) super.setCustomTemplate( customTemplate );
	}

	@Override
	public TextViewElement set( WitherSetter... setters ) {
		super.set( setters );
		return this;
	}

	@Override
	public TextViewElement remove( WitherRemover... functions ) {
		super.remove( functions );
		return this;
	}

	/**
	 * If the target implements {@link ConfigurableTextViewElement} then the text property will be copied.
	 * Else if the target is a {@link ContainerViewElement} the element itself will be added as the first child.
	 */
	@Override
	public void applyTo( ViewElement target ) {
		if ( target instanceof ConfigurableTextViewElement ) {
			( (ConfigurableTextViewElement) target ).setText( text );
		}
		else if ( target instanceof ContainerViewElement ) {
			if ( text != null ) {
				( (ContainerViewElement) target ).addFirstChild( this );
			}
		}
		else {
			throw new IllegalArgumentException( "Unable to configure text on " + target );
		}
	}

	/**
	 * Factory method to create a {@link TextViewElement} for plain xml-escaped text.
	 *
	 * @param text content
	 * @return element
	 */
	public static TextViewElement text( String text ) {
		return new TextViewElement( text, true );
	}

	/**
	 * Alias to {@link #xml(String)}.
	 *
	 * @param html content
	 * @return element
	 */
	public static TextViewElement html( String html ) {
		return xml( html );
	}

	/**
	 * Factory method to create a {@link TextViewElement} for XML content (non-escaped).
	 *
	 * @param xml content
	 * @return element
	 */
	public static TextViewElement xml( String xml ) {
		return new TextViewElement( xml, false );
	}
}


