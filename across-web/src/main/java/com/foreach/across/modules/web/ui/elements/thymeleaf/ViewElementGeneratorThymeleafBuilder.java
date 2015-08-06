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

import com.foreach.across.modules.web.thymeleaf.HtmlIdStore;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public class ViewElementGeneratorThymeleafBuilder implements ViewElementThymeleafBuilder<ViewElementGenerator<?, ?>>
{
	@Override
	public List<Node> buildNodes( ViewElementGenerator<?, ?> container,
	                              Arguments arguments,
	                              ViewElementNodeFactory componentElementProcessor ) {
		List<Node> list = new ArrayList<>();

		HtmlIdStore originalIdStore = HtmlIdStore.fetch( arguments );

		try {
			for ( ViewElement child : container ) {
				if ( child != null ) {
					if ( !container.isBuilderItemTemplate() ) {
						HtmlIdStore.store( originalIdStore.createNew(), arguments );
					}

					list.addAll( componentElementProcessor.buildNodes( child, arguments ) );
				}
			}
		}
		finally {
			// Put back the original id store
			HtmlIdStore.store( originalIdStore, arguments );
		}

		return list;
	}
}
