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
package com.foreach.across.modules.web.ui;

import com.foreach.across.core.support.WritableAttributes;
import com.foreach.across.modules.web.resource.WebResourceUtils;

import java.util.Optional;

public interface ViewElementBuilderContext extends WritableAttributes
{
	/**
	 * Will build a link using the {@link com.foreach.across.modules.web.context.WebAppLinkBuilder} attribute
	 * that is present on this context.  If none is present, the baseLink will remain unmodified.
	 *
	 * @param baseLink to process
	 * @return processed link
	 */
	String buildLink( String baseLink );

	/**
	 * Fetches the global {@link ViewElementBuilderContext}.  Can be bound to the local thread using
	 * {@link ViewElementBuilderContextHolder} or - lacking that - to the request attributes.
	 *
	 * @return optional buildercontext
	 */
	static Optional<ViewElementBuilderContext> retrieveGlobalBuilderContext() {
		return Optional.ofNullable(
				ViewElementBuilderContextHolder
						.getViewElementBuilderContext()
						.orElseGet( () -> WebResourceUtils.currentViewElementBuilderContext().orElse( null ) )
		);
	}
}
