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

import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.unbescape.html.HtmlEscape;

public class TextViewElementThymeleafBuilder implements ViewElementThymeleafBuilder<TextViewElement>
{
	@Override
	public IModel buildNodes( TextViewElement viewElement,
	                          ITemplateContext context,
	                          ViewElementNodeFactory componentElementProcessor ) {
		String content = StringUtils.defaultString( viewElement.getText() );
		String html = viewElement.isEscapeXml() ? HtmlEscape.escapeHtml4Xml( content ) : content;

		IModelFactory modelFactory = context.getModelFactory();

		IModel model = modelFactory.createModel();
		model.add( modelFactory.createText( html ) );
		//Text text = new Text( html, null, null, true );

		return model;
	}
}
