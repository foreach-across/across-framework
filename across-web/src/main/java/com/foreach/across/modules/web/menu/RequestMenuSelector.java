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

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

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

	private int maxScore = 0;
	private Menu itemFound = null;

	private final String fullUrl;
	private final String servletPath;
	private final String servletPathWithQueryString;

	public RequestMenuSelector( HttpServletRequest request ) {
		UriComponents uriComponents = ServletUriComponentsBuilder.fromRequest( request ).build();
		servletPath = stripContextPath( request, uriComponents.getPath() );
		String url = uriComponents.toUriString();
		String pathWithQueryString = servletPath;
		String qs = uriComponents.getQuery();

		if ( !StringUtils.isBlank( qs ) ) {
			pathWithQueryString += "?" + qs;
		}

		fullUrl = url;
		servletPathWithQueryString = pathWithQueryString;
	}

	private String stripContextPath( HttpServletRequest request, String path ) {
		String contextPath = request.getContextPath();
		if ( contextPath != null && contextPath.length() > 1 ) {
			return StringUtils.removeStart( path, contextPath );
		}

		return path;
	}

	/**
	 * @param fullUrl                    Full url - including schema and querystring.
	 * @param servletPath                Path within the application, excluding the querystring.
	 * @param servletPathWithQueryString Path within the application including the querystring.
	 */
	public RequestMenuSelector( String fullUrl, String servletPath, String servletPathWithQueryString ) {
		this.fullUrl = fullUrl;
		this.servletPath = servletPath;
		this.servletPathWithQueryString = servletPathWithQueryString;
	}

	public synchronized Menu find( Menu menu ) {
		maxScore = 0;
		itemFound = null;

		scoreItems( menu );

		return itemFound;
	}

	private void scoreItems( Menu menu ) {
		score( menu );
		for ( Menu item : menu.getItems() ) {
			scoreItems( item );
		}
	}

	private void score( Menu menu ) {
		int calculated = 0;

		AtomicInteger score = new AtomicInteger( calculated );

		Collection<String> stringsToMatch = new LinkedList<>();
		stringsToMatch.add( menu.getUrl() );
		stringsToMatch.add( menu.getPath() );

		Collection<String> additionalStringsToMatch = menu.getAttribute( ATTRIBUTE_MATCHERS );

		if ( additionalStringsToMatch != null ) {
			stringsToMatch.addAll( additionalStringsToMatch );
		}

		for ( String stringToMatch : stringsToMatch ) {
			match( fullUrl, stringToMatch, 9, 7, score );
			match( servletPathWithQueryString, stringToMatch, 9, 7, score );
			match( servletPath, stringToMatch, 8, 6, score );
		}

		calculated = score.intValue();

		if ( calculated > 0 ) {
			calculated += ( menu.getLevel() + 1000000 ) * 10;
		}

		if ( calculated > maxScore ) {
			maxScore = calculated;
			itemFound = menu;
		}
	}

	private void match( String url, String pathToTest, int equalsScore, int startsWithScore, AtomicInteger total ) {
		if ( StringUtils.equals( url, pathToTest ) && total.intValue() < equalsScore ) {
			total.set( equalsScore * 1000 );
		}
		else if ( StringUtils.startsWith( url, pathToTest ) && total.intValue() < startsWithScore ) {
			total.set( startsWithScore * 1000 + pathToTest.length() );
		}
	}
}
