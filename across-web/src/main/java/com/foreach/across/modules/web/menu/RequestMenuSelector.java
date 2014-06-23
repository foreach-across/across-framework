package com.foreach.across.modules.web.menu;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
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
	 * Value should be string or array of strings.
	 */
	public static final String ATTRIBUTE_MATCHERS = RequestMenuSelector.class.getCanonicalName() + ".MATCHERS";

	private final HttpServletRequest request;
	private final UrlPathHelper urlPathHelper;

	private int maxScore = 0;
	private Menu itemFound = null;

	private String fullUrl;
	private String servletPath;
	private String servletPathWithQueryString;

	public RequestMenuSelector( HttpServletRequest request ) {
		this.request = request;

		urlPathHelper = new UrlPathHelper();
	}

	public synchronized Menu find( Menu menu ) {
		maxScore = 0;
		itemFound = null;

		fullUrl = request.getRequestURL().toString();
		servletPath = urlPathHelper.getPathWithinApplication( request );
		servletPathWithQueryString = servletPath;

		String qs = request.getQueryString();

		if ( !StringUtils.isBlank( qs ) ) {
			fullUrl += "?" + qs;
			servletPathWithQueryString += "?" + qs;
		}

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

		Object additionalStringsToMatch = menu.getAttribute( ATTRIBUTE_MATCHERS );

		if ( additionalStringsToMatch != null ) {
			if ( additionalStringsToMatch instanceof String ) {
				stringsToMatch.add( (String) additionalStringsToMatch );
			}
			else if ( additionalStringsToMatch instanceof String[] ) {
				stringsToMatch.addAll( Arrays.asList( (String[]) additionalStringsToMatch ) );
			}
			else {
				throw new RuntimeException(
						"Menu attribute " + ATTRIBUTE_MATCHERS + " only supports String or String[] values" );
			}
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
		if ( StringUtils.equals( url, pathToTest ) ) {
			if ( total.intValue() < equalsScore ) {
				total.set( equalsScore );
			}
		}
		else if ( StringUtils.startsWith( url, pathToTest ) ) {
			if ( total.intValue() < startsWithScore ) {
				total.set( startsWithScore );
			}
		}
	}
}
