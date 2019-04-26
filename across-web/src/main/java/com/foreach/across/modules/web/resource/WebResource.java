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

import com.foreach.across.modules.web.resource.elements.CssWebResourceBuilder;
import com.foreach.across.modules.web.resource.elements.JavascriptWebResourceBuilder;
import com.foreach.across.modules.web.resource.elements.LinkWebResourceBuilder;
import com.foreach.across.modules.web.resource.elements.MetaWebResourceBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * <p>Represents a single entry in the WebResourceRegistry.</p>
 * <p>All constants are deliberately Strings so they can easily be used in different view
 * layers and custom values can be added in other modules.</p>
 * <p>
 * As of version {@code 3.2.0} the {@code WebResource} is mainly used as a static facade
 * for commonly used web resource types (eg. {@link #css()} and {@link #javascript()}) and
 * for the global constants of default buckets. Creating instances of {@code WebResource}
 * is deprecated and only kept for backwards compatibility.
 * </p>
 *
 * @see WebResourceRegistry
 */
public class WebResource
{
	/**
	 * Default buckets of web resources.
	 */
	public static final String HEAD = "head";
	public static final String CSS = "css";
	public static final String JAVASCRIPT = "javascript";
	public static final String JAVASCRIPT_PAGE_END = "javascript-page-end";

	/**
	 * Used for data that should be serialized and passed to the client (usually as json).
	 *
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	public static final String DATA = "data";

	/**
	 * Inline resource - entire content
	 *
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	public static final String INLINE = "inline";

	/**
	 * External resource - usually an absolute link
	 *
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	public static final String EXTERNAL = "external";

	/**
	 * Relative to the context/controller being rendered - this is usually the default.
	 *
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	public static final String RELATIVE = "relative";

	/**
	 * Embedded resource in the views directory - these usually find translated into a path using a
	 * {@link com.foreach.across.modules.web.resource.WebResourceTranslator}.
	 *
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	public static final String VIEWS = "views";

	/**
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	@Getter
	@Setter
	private String key, type, location;

	/**
	 * @deprecated since 3.2.0 - use {@link WebResource#css(String)} or {@link WebResource#javascript(String)} instead
	 */
	@Deprecated
	@Getter
	@Setter
	private Object data;

	@Deprecated
	public boolean hasKey() {
		return key != null;
	}

	/**
	 * @see WebResourceRegistry
	 * @deprecated since 3.2.0- replaced by {@link WebResourceReference}
	 */
	@Deprecated
	public WebResource( String type, String key, Object data, String location ) {
		this.type = type;
		this.key = key;
		this.data = data;
		this.location = location;
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a {@code <script>}
	 * element that registers the data argument as global JSON data. See {@link JavascriptWebResourceBuilder#globalJsonData(String, Object)}
	 * for more details.
	 *
	 * @param key  qualified key under which to register the data
	 * @param data object that should be converted to json
	 * @return javascript element builder
	 */
	public static JavascriptWebResourceBuilder globalJsonData( @NonNull String key, Object data ) {
		return javascript().inline( JavascriptWebResourceBuilder.globalJsonData( key, data ) );
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a script tag for external resources
	 */
	public static JavascriptWebResourceBuilder javascript( @NonNull String url ) {
		return javascript().url( url );
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a script tag and can be used for inline or json
	 */
	public static JavascriptWebResourceBuilder javascript() {
		return new JavascriptWebResourceBuilder();
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a link or style tag, depending if inline or url is used
	 */
	public static CssWebResourceBuilder css() {
		return new CssWebResourceBuilder();
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a link tag
	 */
	public static CssWebResourceBuilder css( @NonNull String url ) {
		return css().url( url );
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a link tag for the url specified.
	 */
	public static LinkWebResourceBuilder link( @NonNull String url ) {
		return link().url( url );
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a link tag.
	 */
	public static LinkWebResourceBuilder link() {
		return new LinkWebResourceBuilder();
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a meta tag for the attribute name.
	 */
	public static MetaWebResourceBuilder meta( @NonNull String attributeName ) {
		return meta().metaName( attributeName );
	}

	/**
	 * A {@link com.foreach.across.modules.web.ui.ViewElementBuilder} which generates a meta tag.
	 */
	public static MetaWebResourceBuilder meta() {
		return new MetaWebResourceBuilder();
	}
}
