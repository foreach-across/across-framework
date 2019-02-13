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
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Accessors(fluent = true, chain = true)
@Setter
@Getter
public class JavascriptWebResourceBuilder extends AbstractWebResourceBuilder
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
		ContainerViewElement container = new ContainerViewElement();
		NodeViewElement script = new NodeViewElement( "script" );

		if ( StringUtils.isNotBlank( url ) ) {
			script.setAttribute( "src", url );
			if ( async ) {
				script.setAttribute( "async", "" );
			}
			if ( defer ) {
				script.setAttribute( "defer", "" );
			}
		}
		else {
			if ( StringUtils.isNotBlank( inline ) ) {
				script.addChild( TextViewElement.html( inline ) );
			}
			else {
				if ( data != null ) {
					script.addChild( TextViewElement.html( "(function ( Across ) {\n" +
							                                       "var data=" + new ObjectMapper().writeValueAsString( data ) + ";\n" +
							                                       "for(var key in data) Across[key] = data[key];\n" +
							                                       "        })( window.Across = window.Across || {} );\n" ) );
				}
			}

		}
		if ( StringUtils.isNotBlank( type ) ) {
			script.setAttribute( "type", type );
		}
		else {
			script.setAttribute( "type", "text/javascript" );
		}

		container.addChild( script );
		return container;
	}
}
