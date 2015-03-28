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
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElementSupport;
import org.thymeleaf.Arguments;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper that keeps track of the generated html ids for {@link com.foreach.across.modules.web.ui.elements.NodeViewElementSupport}
 * instance (simple nodes).  This ensures that other elements (eg. labels) can retrieve the render time generated id.
 *
 * @author Arne Vandamme
 */
public class HtmlIdStore
{
	private final Arguments arguments;
	private final Map<ViewElement, String> generatedIds;

	public HtmlIdStore( Arguments arguments ) {
		this( arguments, new HashMap<ViewElement, String>() );
	}

	private HtmlIdStore( Arguments arguments, Map<ViewElement, String> generatedIds ) {
		this.arguments = arguments;
		this.generatedIds = generatedIds;
	}

	/**
	 * Retrieve the generated id for a {@link com.foreach.across.modules.web.ui.ViewElement}.  If the instance
	 * does not implement {@link com.foreach.across.modules.web.ui.elements.NodeViewElementSupport} null will be
	 * returned.
	 * <p>
	 * If an id is configured on the element, a unique id will be generated and stored for subsequent retrieval.</p>
	 *
	 * @param control element for which to retrieve the id
	 * @return id or null if no id is available
	 */
	public String retrieveHtmlId( ViewElement control ) {
		String htmlId = null;

		if ( control instanceof NodeViewElementSupport ) {
			htmlId = generatedIds.get( control );

			if ( htmlId == null ) {
				htmlId = ( (NodeViewElementSupport) control ).getHtmlId();

				if ( htmlId != null ) {
					int idCount = arguments.getAndIncrementIDSeq( htmlId );
					if ( idCount > 1 ) {
						htmlId = htmlId + ( idCount - 1 );
					}

					generatedIds.put( control, htmlId );
				}
			}
		}

		return htmlId;
	}

	/**
	 * Create a new instance by copying the the original generated elements.
	 * New elements added will not be added to the original set, making this store safe to use within
	 * a single iteration that creates new ViewElements.
	 *
	 * @return new instance of a HtmlIdStore
	 */
	public HtmlIdStore createNew() {
		return new HtmlIdStore( arguments, new HashMap<>( generatedIds ) );
	}

	public static HtmlIdStore fetch( Arguments arguments ) {
		return (HtmlIdStore) arguments.getExpressionObjects().get( AcrossWebDialect.HTML_ID_STORE );
	}

	public static void store( HtmlIdStore idStore, Arguments arguments ) {
		arguments.getExpressionObjects().put( AcrossWebDialect.HTML_ID_STORE, idStore );
	}
}
