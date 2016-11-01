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
package com.foreach.across.modules.web.ui.elements.thymeleaf;

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;

/**
 * Supports implementations of {@link com.foreach.across.modules.web.ui.elements.AbstractVoidNodeViewElement}
 * and {@link AbstractNodeViewElement}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public abstract class AbstractHtmlViewElementModelWriter<T extends HtmlViewElement>
		implements ViewElementModelWriter<T>
{
	@Override
	public void writeModel( T viewElement, ThymeleafModelBuilder writer ) {
		writeOpenElement( viewElement, writer );
		writeChildren( viewElement, writer );
		writeCloseElement( viewElement, writer );
	}

	protected void writeOpenElement( T viewElement, ThymeleafModelBuilder writer ) {
		writer.addOpenElement( viewElement.getTagName() );
		writer.addAttribute( "id", writer.retrieveHtmlId( viewElement ) );
		viewElement.getAttributes().forEach( writer::addAttribute );
	}

	protected void writeChildren( T viewElement, ThymeleafModelBuilder writer ) {
		if ( viewElement instanceof AbstractNodeViewElement ) {
			( (AbstractNodeViewElement) viewElement ).getChildren().forEach( writer::addViewElement );
		}
	}

	protected void writeCloseElement( T viewElement, ThymeleafModelBuilder writer ) {
		writer.addCloseElement();
	}
}
