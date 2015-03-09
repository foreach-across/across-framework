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
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilder;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.unbescape.html.HtmlEscape;

import java.util.Collections;
import java.util.List;

public class TextViewElementNodeBuilder implements ViewElementNodeBuilder<TextViewElement>
{
	@Override
	public List<Node> buildNodes( TextViewElement viewElement,
	                              Arguments arguments,
	                              ViewElementNodeFactory componentElementProcessor ) {
		String html = viewElement.isEscapeXml()
				? HtmlEscape.escapeHtml4Xml( viewElement.getText() )
				: viewElement.getText();

		Text text = new Text( html, null, null, true );

		return Collections.singletonList( (Node) text );
	}
}
