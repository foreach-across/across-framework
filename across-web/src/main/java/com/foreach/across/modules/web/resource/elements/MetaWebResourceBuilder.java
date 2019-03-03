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

import java.util.Optional;

/**
 * Builder class for creating a {@link ViewElement} of tag <meta>
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class MetaWebResourceBuilder extends AbstractVoidNodeViewElementBuilder<VoidNodeViewElement, MetaWebResourceBuilder> implements WebResourceKeyProvider
{
	/**
	 * The {@code name} attribute for the meta tag, if not set explicitly, will be the same as {@link #name(String)}.
	 */
	private String metaName;
	private String content;
	private String httpEquiv;

	@Override
	public MetaWebResourceBuilder name( String name ) {
		if ( metaName == null ) {
			metaName = name;
		}
		return super.name( name );
	}

	/**
	 * Short hand for a http-equiv refresh <meta> tag
	 */
	public MetaWebResourceBuilder refresh( String refresh ) {
		this.httpEquiv = "refresh";
		this.content = refresh;
		return this;
	}

	@Override
	public Optional<String> getWebResourceKey() {
		if ( StringUtils.isNotBlank( metaName ) ) {
			return Optional.of( metaName );
		}
		if ( StringUtils.isNotBlank( httpEquiv ) ) {
			return Optional.of( httpEquiv );
		}
		return Optional.empty();
	}

	@Override
	public VoidNodeViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		VoidNodeViewElement element = new VoidNodeViewElement( "meta" );

		if ( StringUtils.isNotEmpty( httpEquiv ) ) {
			element.setAttribute( "http-equiv", httpEquiv );
		}

		if ( StringUtils.isNotEmpty( metaName ) ) {
			element.setAttribute( "name", metaName );
		}

		if ( StringUtils.isNotEmpty( content ) ) {
			element.setAttribute( "content", content );
		}

		return apply( element, builderContext );
	}
}
