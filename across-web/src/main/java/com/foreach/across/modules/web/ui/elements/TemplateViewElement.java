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
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link com.foreach.across.modules.web.ui.ViewElement} that simply renders as a custom template.
 * If no {@link #getCustomTemplate()} is defined, this element will output nothing.
 *
 * @author Arne Vandamme
 */
public class TemplateViewElement extends ViewElementSupport
{
	public static final String ELEMENT_TYPE = StandardViewElements.TEMPLATE;

	private Map<String, Object> attributes = new HashMap<>();

	public TemplateViewElement( String name, String customTemplate ) {
		this( customTemplate );
		setName( name );
	}

	public TemplateViewElement( String customTemplate ) {
		this();
		setCustomTemplate( customTemplate );
	}

	public TemplateViewElement() {
		super( ELEMENT_TYPE );
	}

	@Override
	public void setElementType( String elementType ) {
		super.setElementType( elementType );
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes( Map<String, Object> attributes ) {
		Assert.notNull( attributes );
		this.attributes = attributes;
	}

	public void setAttribute( String attributeName, Object attributeValue ) {
		attributes.put( attributeName, attributeValue );
	}

	public void addAttributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
	}

	public void removeAttribute( String attributeName ) {
		attributes.remove( attributeName );
	}

	public Object getAttribute( String attributeName ) {
		return attributes.get( attributeName );
	}

	public <V> V getAttribute( String attributeName, Class<V> expectedType ) {
		return returnIfType( attributes.get( attributeName ), expectedType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	@SuppressWarnings("unchecked")
	protected <V> V returnIfType( Object value, Class<V> elementType ) {
		return elementType.isInstance( value ) ? (V) value : null;
	}
}
