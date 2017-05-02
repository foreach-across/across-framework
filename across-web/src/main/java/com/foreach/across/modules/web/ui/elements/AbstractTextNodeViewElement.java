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

import com.foreach.across.modules.web.ui.ViewElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class that extends {@link AbstractNodeViewElement} with the {@link ConfigurableTextViewElement}.
 * If {@link #setText(String)} is set, a {@link TextViewElement} will be added.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public abstract class AbstractTextNodeViewElement extends AbstractNodeViewElement implements ConfigurableTextViewElement
{
	private String text;

	protected AbstractTextNodeViewElement( String tagName ) {
		super( tagName );
	}

	/**
	 * Configure a simple text body for the alert.  Additional children will be after.
	 *
	 * @param text to set
	 */
	@Override
	public void setText( String text ) {
		this.text = text;
	}

	/**
	 * @return the simple text configured
	 */
	@Override
	public String getText() {
		return text;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || text != null;
	}

	@Override
	public List<ViewElement> getChildren() {
		if ( text != null ) {
			List<ViewElement> children = new ArrayList<>();
			children.add( new TextViewElement( text ) );
			children.addAll( super.getChildren() );
			return children;
		}

		return super.getChildren();
	}
}
