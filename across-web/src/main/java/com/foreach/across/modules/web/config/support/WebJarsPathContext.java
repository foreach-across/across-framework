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
package com.foreach.across.modules.web.config.support;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import lombok.NonNull;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class WebJarsPathContext extends PrefixingPathContext
{
	private final boolean isRelative;

	public WebJarsPathContext( @NonNull String prefix ) {
		super( prefix );

		isRelative = !prefix.contains( "//" );
	}

	@Override
	protected String applyPrefixToPath( String prefix, String path ) {
		int slashPos = path.indexOf( '/' );

		if ( slashPos < 0 ) {
			return super.applyPrefixToPath( prefix, path );
		}

		if ( isRelative ) {
			if ( slashPos == 0 ) {
				return super.applyPrefixToPath( prefix, path );
			}
			else {
				return super.applyPrefixToPath( prefix, path.substring( slashPos ) );
			}
		}

		if ( slashPos == 0 ) {
			return prefix + "/org.webjars" + path;
		}

		return prefix + "/" + ( path.startsWith( "org.webjars" ) ? path : "org.webjars." + path );
	}
}
