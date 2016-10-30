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
import org.thymeleaf.context.ITemplateContext;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper that keeps track of the generated html ids for {@link com.foreach.across.modules.web.ui.elements.HtmlViewElement}
 * instance (simple nodes).  This ensures that other elements (eg. labels) can retrieve the render time generated id.
 *
 * @author Arne Vandamme
 * @since 1.0.0
 */
public class HtmlIdStore
{
	private final Deque<Map<ViewElement, String>> generatedIdMaps = new ArrayDeque<>();

	private Map<ViewElement, String> generatedIds;

	public HtmlIdStore() {
		generatedIds = new HashMap<>();
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
	public String retrieveHtmlId( ITemplateContext context, ViewElement control ) {
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
	 * Increase the level of element id caching.  This will copy the currently cached ids into
	 * a new collection.  Any ids after will only be kept until a call to {@link #decreaseLevel()}.
	 * When that happens, the previous collection will be reset.
	 */
	public void increaseLevel() {
		HashMap<ViewElement, String> newIds = new HashMap<>();
		newIds.putAll( generatedIds );

		generatedIdMaps.push( generatedIds );
		generatedIds = newIds;
	}

	/**
	 * Decrease the level.  This resets the cached element ids to the version that was present before
	 * the last call to {@link #increaseLevel()}.  If the level has never been increased, this method
	 * will remove all cached element ids.
	 */
	public void decreaseLevel() {
		if ( !generatedIdMaps.isEmpty() ) {
			generatedIds = generatedIdMaps.pop();
		}
		else {
			generatedIds.clear();
		}
	}

	/**
	 * Fetch the store from the current template context.
	 *
	 * @param context to get the store from
	 * @return id store instance
	 */
	public static HtmlIdStore fetch( ITemplateContext context ) {
		return (HtmlIdStore) context.getExpressionObjects().getObject( AcrossWebDialect.HTML_ID_STORE );
	}
}
