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

import java.util.Collections;
import java.util.Map;

/**
 * Helper class for relative urls that need a prefix. Any path passed to this instance will be prefixed
 * unless an exception is defined. Possible exceptions are:
 * <ul>
 * <li>path starts with <b>!</b>: only exclamation mark will be removed, no prefixing will be done (supress prefixing)</li>
 * <li>path starts with <b>~</b>: will not be modified</li>
 * <li>path contains <b>://</b>: considered absolute url - will not be modified</li>
 * <li>path starts with <b>@prefixer:</b>: will be re-routed to the prefixer with that name if present</li>
 * </ul>
 * The helper also supports <b>redirect:</b> and <b>forward:</b> urls.
 * <br />
 * Optionally an additional map of named prefixers can be added {@link #setNamedPrefixMap(java.util.Map)}.  Urls containing
 * one of these names in the correct format (eg: <b>@somePrefixer:/myPath</b>) will be prefixed by that prefixer
 * instead of the current one.
 *
 * @see com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping
 */
public class PrefixingPathContext implements WebAppPathResolver
{
	/* Suppress prefixing character */
	public static final String SUPPRESS = "!";

	private static final String REDIRECT = "redirect:";
	private static final String FORWARD = "forward:";

	private final String prefix;
	private final String[] ignoredPrefixes = new String[] { REDIRECT, FORWARD };

	private Map<String, PrefixingPathContext> namedPrefixers = Collections.emptyMap();

	public PrefixingPathContext( @NonNull String prefix ) {
		if ( prefix.length() > 0 ) {
			if ( prefix.endsWith( "/" ) ) {
				this.prefix = prefix.substring( 0, prefix.length() - 1 );
			}
			else {
				this.prefix = prefix;
			}
		}
		else {
			this.prefix = StringUtils.EMPTY;
		}
	}

	/**
	 * Sets a collection of prefixing contexts with a name.  Any paths starting with {NAME} will
	 * find re-routed to the named prefixer instead of handled by the current prefixing context.
	 *
	 * @param namedPrefixers Map of namedPrefixers, should not be null.
	 */
	public void setNamedPrefixMap( @NonNull Map<String, PrefixingPathContext> namedPrefixers ) {
		this.namedPrefixers = namedPrefixers;
	}

	/**
	 * @return Root of the prefixed context (no sub path).
	 */
	public String getRoot() {
		return path( StringUtils.EMPTY );
	}

	public String getPathPrefix() {
		return prefix;
	}

	@Override
	public String path( String path ) {
		if ( StringUtils.startsWith( path, "#" ) ) {
			return path;
		}

		for ( String ignoredPrefix : ignoredPrefixes ) {
			if ( path.startsWith( ignoredPrefix ) ) {
				return ignoredPrefix + prefix( path.substring( ignoredPrefix.length() ) );
			}
		}

		return prefix( path );
	}

	@Override
	public String redirect( String path ) {
		return REDIRECT + path( path.startsWith( REDIRECT ) ? path.substring( 9 ) : path );
	}

	private String prefix( String path ) {
		if ( path.startsWith( SUPPRESS ) ) {
			return path.substring( 1 );
		}

		if ( path.startsWith( "~" ) || path.startsWith( "//" ) || path.contains( "://" ) ) {
			return path;
		}

		if ( !namedPrefixers.isEmpty() && path.startsWith( "@" ) ) {
			for ( Map.Entry<String, PrefixingPathContext> prefixer : namedPrefixers.entrySet() ) {
				String prefixerName = "@" + prefixer.getKey() + ":";
				if ( path.startsWith( prefixerName ) ) {
					return prefixer.getValue().prefix( path.replaceFirst( prefixerName, "" ) );
				}
			}
		}

		return applyPrefixToPath( prefix, path );
	}

	protected String applyPrefixToPath( String prefix, String path ) {
		if ( path.startsWith( "/" ) ) {
			return prefix + path;
		}
		else {
			return prefix + "/" + path;
		}
	}
}
