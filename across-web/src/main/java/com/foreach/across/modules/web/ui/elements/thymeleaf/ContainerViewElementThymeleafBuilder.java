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
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class ContainerViewElementThymeleafBuilder implements ViewElementThymeleafBuilder<ContainerViewElement>
{
	@Override
	public List<Node> buildNodes( ContainerViewElement container,
	                              ITemplateContext context,
	                              ViewElementNodeFactory componentElementProcessor ) {
		List<Node> list = new ArrayList<>();

		container.getChildren().forEach(
				c -> list.addAll( componentElementProcessor.buildNodes( c, context ) )
		);

		return list;
	}
}
