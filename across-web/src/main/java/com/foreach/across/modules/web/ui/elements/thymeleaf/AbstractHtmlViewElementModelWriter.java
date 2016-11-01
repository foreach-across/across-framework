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
	public void writeModel( T viewElement, ThymeleafModelBuilder model ) {
		writeOpenElement( viewElement, model );
		writeChildren( viewElement, model );
		writeCloseElement( viewElement, model );
	}

	protected void writeOpenElement( T viewElement, ThymeleafModelBuilder model ) {
		model.addOpenElement( viewElement.getTagName() );
		model.addAttribute( "id", model.retrieveHtmlId( viewElement ) );
		viewElement.getAttributes().forEach( model::addAttribute );
	}

	protected void writeChildren( T viewElement, ThymeleafModelBuilder model ) {
		if ( viewElement instanceof AbstractNodeViewElement ) {
			( (AbstractNodeViewElement) viewElement ).getChildren().forEach( model::addViewElement );
		}
	}

	protected void writeCloseElement( T viewElement, ThymeleafModelBuilder model ) {
		model.addCloseElement();
	}
}
