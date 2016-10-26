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
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import org.apache.commons.lang3.NotImplementedException;
import org.thymeleaf.context.ITemplateContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper that keeps track of the generated html ids for {@link com.foreach.across.modules.web.ui.elements.HtmlViewElement}
 * instance (simple nodes).  This ensures that other elements (eg. labels) can retrieve the render time generated id.
 *
 * @author Arne Vandamme
 */
public class HtmlIdStore
{
	private final ITemplateContext context;
	private final Map<ViewElement, String> generatedIds;

	public HtmlIdStore( ITemplateContext context ) {
		this( context, new HashMap<ViewElement, String>() );
	}

	private HtmlIdStore( ITemplateContext context, Map<ViewElement, String> generatedIds ) {
		this.context = context;
		this.generatedIds = generatedIds;
	}

	public static HtmlIdStore fetch( ITemplateContext context ) {
		return (HtmlIdStore) context.getExpressionObjects().getObject( AcrossWebDialect.HTML_ID_STORE );
	}

	public static void store( HtmlIdStore idStore, ITemplateContext context ) {
		//TODO: TH3
		throw new NotImplementedException( "a" );
		//context.getExpressionObjects().put( AcrossWebDialect.HTML_ID_STORE, idStore );
	}

	/**
	 * Retrieve the generated id for a {@link com.foreach.across.modules.web.ui.ViewElement}.  If the instance
	 * does not implement {@link com.foreach.across.modules.web.ui.elements.HtmlViewElement} null will be
	 * returned.
	 * <p>
	 * If an id is configured on the element, a unique id will be generated and stored for subsequent retrieval.</p>
	 *
	 * @param control element for which to retrieve the id
	 * @return id or null if no id is available
	 */
	public String retrieveHtmlId( ViewElement control ) {
		String htmlId = null;

		if ( control instanceof HtmlViewElement ) {
			htmlId = generatedIds.get( control );

			if ( htmlId == null ) {
				htmlId = ( (HtmlViewElement) control ).getHtmlId();

				if ( htmlId != null ) {
					int idCount = context.getIdentifierSequences().getAndIncrementIDSeq( htmlId );
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
	 * a single iteration that creates new {@link ViewElement}s.
	 *
	 * @return new instance of a HtmlIdStore
	 */
	public HtmlIdStore createNew() {
		return new HtmlIdStore( context, new HashMap<>( generatedIds ) );
	}
}
