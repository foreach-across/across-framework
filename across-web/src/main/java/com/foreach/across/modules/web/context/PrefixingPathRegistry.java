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
package com.foreach.across.modules.web.context;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 */
public class PrefixingPathRegistry implements WebAppPathResolver
{
	public static final String DEFAULT = "acrossWeb";

	private final Map<String, PrefixingPathContext> namedPrefixes = new HashMap<>();
	private final PrefixingPathContext defaultContext = new PrefixingPathContext( StringUtils.EMPTY );

	public PrefixingPathRegistry() {
		add( DEFAULT, defaultContext );
	}

	/**
	 * Adds a {@link com.foreach.across.modules.web.context.PrefixingPathContext} to this registry.
	 * This will set the registry as namedDescriptorMap on the context by calling
	 * {@link com.foreach.across.modules.web.context.PrefixingPathContext#setNamedPrefixMap(java.util.Map)}.
	 *
	 * @param name    name that can be used to refer to this context
	 * @param context implementation
	 */
	public void add( @NonNull String name, @NonNull PrefixingPathContext context ) {
		namedPrefixes.put( name, context );
		context.setNamedPrefixMap( namedPrefixes );
	}

	public PrefixingPathContext get( String name ) {
		return namedPrefixes.get( name );
	}

	public void remove( String name ) {
		namedPrefixes.remove( name );
	}

	@Override
	public String path( String path ) {
		return defaultContext.path( path );
	}

	@Override
	public String redirect( String path ) {
		return defaultContext.redirect( path );
	}
}
