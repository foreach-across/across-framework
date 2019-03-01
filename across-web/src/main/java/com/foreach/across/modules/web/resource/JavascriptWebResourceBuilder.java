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

import java.util.function.Function;

/**
 * Builder class for creating a {@link ViewElement} of tag <script>
 *
 * @author Marc Vanbrabant
 * @since 3.2.0
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

	/**
	 * Data-writer function that should be used to convert a data object (when specified)
	 * to the {@code ViewElement} that should be rendered inside
	 * <p/>
	 * Defaults to JSON writing of the data and assigning it as a global "Across" javascript variable.
	 * This was done for compatibility purposes.
	 */
	private Function<Object, String> dataWriter;

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

	@Override
	protected MutableViewElement createElement( @NonNull ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement( "script" );

		if ( StringUtils.isNotBlank( url ) ) {
			element.setAttribute( "src", builderContext.buildLink( url ) );
			if ( async ) {
				element.setAttribute( "async", "async" );
			}
			if ( defer ) {
				element.setAttribute( "defer", "defer" );
			}
		}
		else {
			if ( StringUtils.isNotBlank( inline ) ) {
				element.addChild( TextViewElement.html( inline ) );
			}
			else {
				if ( data != null && dataWriter != null ) {
					element.addChild( TextViewElement.html( dataWriter.apply( data ) ) );
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

	/**
	 * Returns a {@code Function} for {@link #dataWriter(Function)} which converts the data object
	 * to JSON variables and register them as global variables on the window. The root key for
	 * the global variables can be specified.
	 * <p/>
	 * Every property or key
	 *
	 * @param globalKey root key for the data object
	 * @return data writer function
	 */
	public static Function<Object, ViewElement> dataToWindowGlobalVariables( @NonNull String globalKey ) {
		return null;
	}
}
