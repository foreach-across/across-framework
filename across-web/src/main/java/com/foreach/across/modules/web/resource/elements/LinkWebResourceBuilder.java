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

import com.foreach.across.modules.web.resource.WebResourceKeyProvider;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.VoidNodeViewElement;
import com.foreach.across.modules.web.ui.elements.builder.AbstractVoidNodeViewElementBuilder;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.Optional;

/**
 * Builder class for creating a {@link ViewElement} that represents a generic <link> tag.
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class LinkWebResourceBuilder extends AbstractVoidNodeViewElementBuilder<VoidNodeViewElement, LinkWebResourceBuilder> implements WebResourceKeyProvider
{
	private String url;
	private String rel;
	private String type;
	private String media;

	/**
	 * @return a default web resource key for this link element, identical to the value of {@link #url(String)}
	 */
	@Override
	public Optional<String> getWebResourceKey() {
		if ( url != null ) {
			return Optional.of( url );
		}
		return Optional.empty();
	}

	public LinkWebResourceBuilder type( @NonNull MediaType mediaType ) {
		return type( mediaType.toString() );
	}

	public LinkWebResourceBuilder type( String type ) {
		this.type = type;
		return this;
	}

	public LinkWebResourceBuilder crossOrigin( String crossOrigin ) {
		return attribute( "crossorigin", crossOrigin );
	}

	@Override
	protected VoidNodeViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		VoidNodeViewElement element = new VoidNodeViewElement( "link" );

		if ( StringUtils.isNotEmpty( url ) ) {
			element.setAttribute( "href", builderContext.buildLink( url ) );
		}

		element.setAttribute( "rel", rel );
		element.setAttribute( "type", type );
		element.setAttribute( "media", media );

		return apply( element, builderContext );
	}
}
