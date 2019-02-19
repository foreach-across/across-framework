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
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * Builder class for creating a {@link ViewElement} of tag <link> or <style>
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
 */
@Accessors(fluent = true, chain = true)
@Setter
public class CssWebResourceBuilder extends ViewElementBuilderSupport
{
	private String url;
	private String rel;
	private String type;
	private String inline;

	@Override
	public MutableViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		NodeViewElement element;

		if ( StringUtils.isNotBlank( url ) ) {
			element = new NodeViewElement( "link" );
			element.setAttribute( "href", builderContext.buildLink( url ) );

			if ( StringUtils.isNotBlank( rel ) ) {
				element.setAttribute( "rel", rel );
			}
			else {
				element.setAttribute( "rel", "stylesheet" );
			}
		}
		else {
			element = new NodeViewElement( "style" );
			if ( StringUtils.isNotBlank( inline ) ) {
				element.addChild( TextViewElement.html( inline ) );
			}
		}

		if ( StringUtils.isNotBlank( type ) ) {
			element.setAttribute( "type", type );
		}
		else {
			element.setAttribute( "type", "text/css" );
		}
		return element;
	}
}
