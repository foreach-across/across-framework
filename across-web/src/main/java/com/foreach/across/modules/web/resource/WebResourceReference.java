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
package com.foreach.across.modules.web.resource;

import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.Ordered;

/**
 * <p>Represents a single entry in a bucket in the {@link WebResourceRegistry}.</p>
 * <p>The key is unique within a bucket and ordering can be applied using the before, after and order fields.</p>
 * <p>Ordering is handled by {@link WebResourceSorter}.</p>
 *
 * @see WebResourceRegistry
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@Getter
public class WebResourceReference
{
	private final ViewElementBuilder viewElementBuilder;
	private final String key;
	private final String before;
	private final String after;
	private final int order;

	@Deprecated
	@Getter(AccessLevel.PACKAGE)
	private final WebResource resource;

	public WebResourceReference( @NonNull ViewElementBuilder viewElementBuilder,
	                             @NonNull String key,
	                             String before,
	                             String after,
	                             Integer order ) {
		this( viewElementBuilder, key, before, after, order, null );
	}

	@Deprecated
	WebResourceReference( ViewElementBuilder viewElementBuilder,
	                      String key,
	                      String before,
	                      String after,
	                      Integer order,
	                      WebResource resource ) {
		this.viewElementBuilder = viewElementBuilder;
		this.key = key;
		this.before = before;
		this.after = after;
		this.order = order == null ? Ordered.HIGHEST_PRECEDENCE + 1000 : order;
		this.resource = resource;
	}
}
