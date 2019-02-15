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

package com.foreach.across.modules.web.menu;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

/**
 * Will search a menu for the item that best matches with the requested url.
 * A fallback matching is performed:
 * <ol>
 * <li>lowest item with exact url or path</li>
 * <li>lowest item with url or path without querystring</li>
 * <li>lowest item with longest prefix of the requested url</li>
 * </ol>
 */
public class RequestMenuSelector implements MenuSelector
{
	/**
	 * Attribute on a menu to provide extra matching apart from url and standard path.
	 * Value should be a collection of strings.
	 */
	public static final String ATTRIBUTE_MATCHERS = RequestMenuSelector.class.getCanonicalName() + ".MATCHERS";

	private final String servletPath;
	private final String servletPathWithQueryString;

	private final LookupPath absoluteLookup;
	private final LookupPath relativeLookup;

	public RequestMenuSelector( HttpServletRequest request ) {
		UriComponents uriComponents = ServletUriComponentsBuilder.fromRequest( request ).build();
		servletPath = stripContextPath( request, uriComponents.getPath() );
		String url = uriComponents.toUriString();
		String pathWithQueryString = servletPath;
		String qs = uriComponents.getQuery();

		if ( !StringUtils.isBlank( qs ) ) {
			pathWithQueryString += "?" + qs;
		}

		if ( !StringUtils.isBlank( uriComponents.getFragment() ) ) {
			pathWithQueryString += "#" + uriComponents.getFragment();
		}

		servletPathWithQueryString = pathWithQueryString;

		absoluteLookup = LookupPath.parse( url );
		relativeLookup = LookupPath.parse( servletPathWithQueryString );
	}

	/**
	 * @param fullUrl                    Full url - including schema and querystring.
	 * @param servletPath                Path within the application, excluding the querystring.
	 * @param servletPathWithQueryString Path within the application including the querystring.
	 */
	public RequestMenuSelector( String fullUrl, String servletPath, String servletPathWithQueryString ) {
		this.servletPath = servletPath;
		this.servletPathWithQueryString = servletPathWithQueryString;

		absoluteLookup = LookupPath.parse( fullUrl );
		relativeLookup = LookupPath.parse( servletPathWithQueryString );
	}

	private String stripContextPath( HttpServletRequest request, String path ) {
		String contextPath = request.getContextPath();
		if ( contextPath != null && contextPath.length() > 1 ) {
			return StringUtils.removeStart( path, contextPath );
		}

		return path;
	}

	public synchronized Menu find( Menu menu ) {
		int maxScore = LookupPath.NO_MATCH;
		Menu itemFound = null;
		Deque<Menu> queue = new ArrayDeque<>();
		queue.add( menu );

		while ( !queue.isEmpty() ) {
			Menu item = queue.pop();

			int score = calculateScore( item );

			if ( score > LookupPath.NO_MATCH ) {
				if ( score == LookupPath.EXACT_MATCH ) {
					maxScore = score;
					itemFound = item;
					queue.clear();
				}
				else if ( score >= maxScore ) {
					maxScore = score;
					itemFound = item;
				}
			}

			queue.addAll( item.getItems() );
		}

		return itemFound;
	}

	private int calculateScore( Menu menu ) {
		Collection<String> stringsToMatch = new ArrayList<>( 3 );
		if ( menu.hasUrl() ) {
			stringsToMatch.add( menu.getUrl() );
		}
		if ( StringUtils.isNotEmpty( menu.getPath() ) ) {
			stringsToMatch.add( menu.getPath() );
		}

		Collection<String> additionalStringsToMatch = menu.getAttribute( ATTRIBUTE_MATCHERS );

		int itemScore = LookupPath.NO_MATCH;

		if ( additionalStringsToMatch != null ) {
			stringsToMatch.addAll( additionalStringsToMatch );
		}

		for ( String stringToMatch : stringsToMatch ) {
			LookupPath path = LookupPath.parse( stringToMatch );

			int absoluteScore = absoluteLookup.calculateScore( path );

			if ( absoluteScore == LookupPath.EXACT_MATCH ) {
				return absoluteScore;
			}

			int relativeScore = relativeLookup.calculateScore( path );

			itemScore = Math.max( itemScore, Math.max( absoluteScore, relativeScore ) );
		}

		return itemScore;
	}

	@Getter
	static class LookupPath
	{
		static final int NO_MATCH = 0;
		static final int EXACT_MATCH = Integer.MAX_VALUE;

		static final int PATH_EQUALS = 20000;
		static final int PATH_STARTS_WITH = 10000;
		static final int QUERY_PARAM_MATCH = 10;

		private String path;
		private String fragment = "";
		private String[] queryParameters = new String[0];

		/**
		 * Calculate a score for how much the current matches another path.
		 * - not matching the 'path' itself gives a score of 0
		 * - matching the 'path' exactly gives a score of 20000
		 * - matching the path partially gives a score of 10000
		 * - every 'query parameter' match adds 10
		 * - if all query parameters match as well as the fragment, this is an exact match
		 * - query parameters are only taken into account if the path matches exactly
		 *
		 * @param other path to match against
		 * @return score
		 */
		int calculateScore( LookupPath other ) {
			int score = NO_MATCH;

			if ( StringUtils.equals( path, other.path ) ) {
				score = PATH_EQUALS;

				int matchingQueryParams = 0;
				for ( String queryParameter : other.getQueryParameters() ) {
					if ( ArrayUtils.contains( queryParameters, queryParameter ) ) {
						matchingQueryParams++;
					}
				}

				if ( other.queryParameters.length > 0 && matchingQueryParams != other.queryParameters.length ) {
					score = NO_MATCH;
				}
				else if ( matchingQueryParams == other.queryParameters.length ) {
					if ( queryParameters.length == other.queryParameters.length ) {
						score = EXACT_MATCH;
					}
					else {
						score += matchingQueryParams * QUERY_PARAM_MATCH;
					}
				}
			}
			else if ( StringUtils.startsWith( path, other.path + "/" ) && other.queryParameters.length == 0 ) {
				score = PATH_STARTS_WITH + other.path.length();
			}

			return score;
		}

		static LookupPath parse( String url ) {
			LookupPath lookup = new LookupPath();

			String[] urlWithFragment = StringUtils.split( url, "#" );

			if ( urlWithFragment != null && urlWithFragment.length > 0 ) {
				String[] pathAndQueryString = StringUtils.split( urlWithFragment[0], "?" );

				lookup.path = pathAndQueryString[0];

				if ( pathAndQueryString.length > 1 ) {
					lookup.queryParameters = StringUtils.split( pathAndQueryString[1], "&" );
				}

				if ( urlWithFragment.length > 1 ) {
					lookup.fragment = urlWithFragment[1];
				}
			}
			else {
				lookup.path = "";
			}

			return lookup;
		}
	}
}
