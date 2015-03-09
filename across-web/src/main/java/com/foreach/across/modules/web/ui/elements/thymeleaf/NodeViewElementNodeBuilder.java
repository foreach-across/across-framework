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
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilder;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NodeViewElementNodeBuilder implements ViewElementNodeBuilder<NodeViewElement>
{
	@Override
	public List<Node> buildNodes( NodeViewElement element,
	                              Arguments arguments,
	                              ViewElementNodeFactory viewElementNodeFactory ) {
		Element node = new Element( element.getTagName() );

		for ( Map.Entry<String, String> attribute : element.getAttributes().entrySet() ) {
			node.setAttribute( attribute.getKey(), attribute.getValue() );
		}

		for ( ViewElement child : element ) {
			for ( Node childNode : viewElementNodeFactory.buildNodes( child, arguments ) ) {
				node.addChild( childNode );
			}
		}

		return Collections.singletonList( (Node) node );
	}
}
