/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.modules.web.resource.WebResourceKeyProvider;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.builder.AbstractNodeViewElementBuilder;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.Optional;

import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.style;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.unescapedText;

/**
 * Builder class for CSS elements that can be represented either as a {@code <link>} tag or an inline {@code <style>}.
 * If an {@link #inline(String)} value is specified, a {@code <style>} tag will be rendered and certain other
 * properties like {@link #rel(String)} and {@link #url(String)} will be ignored.
 * In all other cases a {@code link} tag will be rendered.
 *
 * @author Marc Vanbrabant
 * @see LinkWebResourceBuilder
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class CssWebResourceBuilder extends AbstractNodeViewElementBuilder<NodeViewElement, CssWebResourceBuilder> implements WebResourceKeyProvider
{
	private String url;
	private String rel;

	/**
	 * Style data that should be inlined, will result in a {@code style} tag being rendered.
	 */
	private String inline;

	public CssWebResourceBuilder() {
		rel( "stylesheet" ).type( "text/css" );
	}

	public CssWebResourceBuilder media( String media ) {
		return attribute( "media", media );
	}

	public CssWebResourceBuilder type( @NonNull MediaType mediaType ) {
		return type( mediaType.toString() );
	}

	public CssWebResourceBuilder type( String type ) {
		return attribute( "type", type );
	}

	/**
	 * @return a default web resource key for this css element, either the value of {@link #url(String)} or {@link #inline(String)}
	 */
	@Override
	public Optional<String> getWebResourceKey() {
		if ( inline != null ) {
			return Optional.of( inline );
		}
		if ( url != null ) {
			return Optional.of( url );
		}
		return Optional.empty();
	}

	@Override
	protected NodeViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		NodeViewElement element;

		if ( StringUtils.isNotEmpty( inline ) ) {
			element = style( unescapedText( inline ) );
		}
		else {
			element = new NodeViewElement( "link" );
			if ( StringUtils.isNotEmpty( url ) ) {
				element.setAttribute( "href", builderContext.buildLink( url ) );
			}
			element.setAttribute( "rel", rel );
		}

		return apply( element, builderContext );
	}
}
