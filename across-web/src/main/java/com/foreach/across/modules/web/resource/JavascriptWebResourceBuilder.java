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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * Builder class for creating a {@link ViewElement} of tag <script>
 *
 * @author Marc Vanbrabant
 * @since 3.1.3
 */
@Accessors(fluent = true, chain = true)
@Setter
public class JavascriptWebResourceBuilder extends ViewElementBuilderSupport
{
	private boolean async;
	private boolean defer;
	private String url;
	private String inline;
	private String type;
	private Object data;

	@Override
	@SneakyThrows
	public MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement( "script" );

		if ( StringUtils.isNotBlank( url ) ) {
			element.setAttribute( "src", builderContext.buildLink( url ) );
			if ( async ) {
				element.setAttribute( "async", "" );
			}
			if ( defer ) {
				element.setAttribute( "defer", "" );
			}
		}
		else {
			if ( StringUtils.isNotBlank( inline ) ) {
				element.addChild( TextViewElement.html( inline ) );
			}
			else {
				if ( data != null ) {
					element.addChild( TextViewElement.html( "(function ( Across ) {\n" +
							                                        "var data=" + new ObjectMapper().writeValueAsString( data ) + ";\n" +
							                                        "for(var key in data) Across[key] = data[key];\n" +
							                                        "        })( window.Across = window.Across || {} );\n" ) );
				}
			}

		}
		if ( StringUtils.isNotBlank( type ) ) {
			element.setAttribute( "type", type );
		}
		else {
			element.setAttribute( "type", "text/javascript" );
		}

		return element;
	}
}
