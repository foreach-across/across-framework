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
package com.foreach.across.modules.web.resource.elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.resource.WebResourceKeyProvider;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.AbstractNodeViewElementBuilder;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.Optional;

/**
 * Builder class for creating a {@link ViewElement} of tag <script> which supports either external
 * ({@code src} attribute) or inlined scripts. An inline script is specified using one of the overloaded
 * {@code inline()} methods. Either a static HTML output string is specified, or a custom {@link ViewElementBuilder}
 * which will return the elements that should be inlined.
 * <p/>
 * In effect the latter is identical as doing {@link #add(ViewElementBuilder...)}, the short-hand {@link #inline(String)}
 * resets any previously set {@link #url(String)} value however and has increased contextual readability.
 * <p/>
 * A custom inline data function is available as {@link #globalJsonData(String, Object)}, which registers a custom object
 * as global JSON variables under a specific key. See the function javadoc for more details.
 *
 * @author Marc Vanbrabant
 * @see #globalJsonData(String, Object)
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class JavascriptWebResourceBuilder extends AbstractNodeViewElementBuilder<NodeViewElement, JavascriptWebResourceBuilder> implements WebResourceKeyProvider
{
	private static final String JAVASCRIPT_TEMPLATE = "(function( _data ) { _data[ \"{1}\" ] = {2}; })( window[\"{0}\"] = window[\"{0}\"] || {} );";
	private static final ObjectMapper JSON_DATA_OBJECT_MAPPER = new ObjectMapper();

	private boolean async;
	private boolean defer;
	private String url;
	private ViewElementBuilder inline;

	public JavascriptWebResourceBuilder() {
		type( "text/javascript" );
	}

	public JavascriptWebResourceBuilder type( @NonNull MediaType mediaType ) {
		return type( mediaType.toString() );
	}

	public JavascriptWebResourceBuilder type( String type ) {
		return attribute( "type", type );
	}

	/**
	 * Shorthand for <code>defer(true)</code>
	 */
	public JavascriptWebResourceBuilder defer() {
		this.defer = true;
		return this;
	}

	/**
	 * Shorthand for <code>async(true)</code>
	 */
	public JavascriptWebResourceBuilder async() {
		this.async = true;
		return this;
	}

	/**
	 * Set the {@code src} for this script element. Will remove any
	 * previously set {@link #inline(String)} value.
	 *
	 * @param url for src
	 * @return current builder
	 */
	public JavascriptWebResourceBuilder url( String url ) {
		this.inline = null;
		this.url = url;
		return this;
	}

	/**
	 * Set the {@code inline} script for this script element. Will remove any
	 * previously set {@link #url(String)} value.
	 *
	 * @param inline script value
	 * @return current builder
	 */
	public JavascriptWebResourceBuilder inline( String inline ) {
		this.url = null;
		this.inline = inline != null ? ctx -> TextViewElement.html( inline ) : null;
		return this;
	}

	/**
	 * Set the {@code inline} script for this script element. Will remove any
	 * previously set {@link #url(String)} value.
	 *
	 * @param inline element builder
	 * @return current builder
	 */
	public JavascriptWebResourceBuilder inline( ViewElementBuilder inline ) {
		this.url = null;
		this.inline = inline;
		return this;
	}

	public JavascriptWebResourceBuilder crossOrigin( String crossOrigin ) {
		return attribute( "crossorigin", crossOrigin );
	}

	@Override
	public Optional<String> getWebResourceKey() {
		if ( StringUtils.isNotEmpty( url ) ) {
			return Optional.of( url );
		}
		return Optional.empty();
	}

	@Override
	protected NodeViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement( "script" );

		if ( StringUtils.isNotEmpty( url ) ) {
			element.setAttribute( "src", builderContext.buildLink( url ) );
			if ( async ) {
				element.setAttribute( "async", "async" );
			}
			if ( defer ) {
				element.setAttribute( "defer", "defer" );
			}
		}
		else if ( inline != null ) {
			element.addChild( inline.build( builderContext ) );
		}

		return apply( element, builderContext );
	}

	/**
	 * Renders a client-side javascript snippet that attempts to register the specified {@code data} object
	 * as JSON values on the global {@code window}, under the {@code globalKey} specified.
	 * Only meant for use in a browser environment as it expects the {@code window} variable to be available.
	 * <p/>
	 * It is required that the key contains at least 2 segments separated by a . (dot).
	 * The first segment will be root key for the {@code window} variable, anything past the
	 * first segment will be the sub key.
	 * <p/>
	 * Keys with exactly two segments and no whitespace will be directly accessible.
	 * For example {@code MyApp.Settings} can be accessed from client-side code in the same fashion;
	 * but registering {@code MyApp.Settings.Enabled} would result in {@code MyApp[Settings.Enabled]}
	 * being registered.
	 * <p/>
	 * The data object can be any type, a simple fixed {@link ObjectMapper} will be used to convert it to JSON.
	 * It is not possible to customize the {@code ObjectMapper} used directly.
	 * <p/>
	 * <strong>NOTE:</strong> this function only verifies that the global key contains at least two segments, it
	 * does not validate the key value in any other fashion, nor if the resulting javascript will in fact be executable.
	 * It is advised to use both simple keys and straightforward data objects to pass settings to the client.
	 *
	 * @param globalKey root key for the data object
	 * @return data writer function
	 */
	public static ViewElementBuilder<TextViewElement> globalJsonData( @NonNull String globalKey, Object data ) {
		String rootKey = StringUtils.substringBefore( globalKey, "." );

		if ( globalKey.equals( rootKey ) ) {
			throw new IllegalArgumentException( "Global JSON data requires at least a root key separated with a . (dot). For example: Across.MyKey." );
		}

		return builderContext -> {
			try {
				String js = StringUtils.replace( JAVASCRIPT_TEMPLATE, "{0}", rootKey );
				js = StringUtils.replace( js, "{1}", StringUtils.substringAfter( globalKey, "." ) );
				js = StringUtils.replace( js, "{2}", JSON_DATA_OBJECT_MAPPER.writeValueAsString( data ) );

				return TextViewElement.html( js );
			}
			catch ( JsonProcessingException jpe ) {
				throw new RuntimeException( "Unable to register global JSON data", jpe );
			}
		};
	}
}
