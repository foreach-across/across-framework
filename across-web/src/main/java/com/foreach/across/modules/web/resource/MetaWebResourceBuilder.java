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

import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * Builder class for creating a {@link ViewElement} of tag <meta>
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class MetaWebResourceBuilder extends ViewElementBuilderSupport
{
	private String metaName;
	private String content;
	private String httpEquiv;

	@Override
	public MutableViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement( "meta" );

		if ( StringUtils.isNotBlank( httpEquiv ) ) {
			element.setAttribute( "http-equiv", httpEquiv );
		}
		else {
			if ( StringUtils.isNotBlank( metaName ) ) {
				element.setAttribute( "name", metaName );
			}
		}

		if ( StringUtils.isNotBlank( content ) ) {
			element.setAttribute( "content", content );
		}

		return element;
	}

	/**
	 * Short hand for a http-equiv refresh <meta> tag
	 */
	public MetaWebResourceBuilder refresh( String refresh ) {
		this.httpEquiv = "refresh";
		this.content = refresh;
		return this;
	}
}
