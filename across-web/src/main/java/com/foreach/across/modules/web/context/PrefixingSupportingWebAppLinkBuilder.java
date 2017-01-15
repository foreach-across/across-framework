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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation of {@link WebAppLinkBuilder} that will resolve any prefixed url
 * and url encode it afterwards.  Supports prefix-based urls (eg. @static:/myresource) and
 * avoids urls starting with mailto: or # to be encoded.
 * <p/>
 * Note this implementation requires a {@link HttpServletRequest} and {@link HttpServletResponse}
 * to be available, so it can only be used within a request-bound thread.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class PrefixingSupportingWebAppLinkBuilder implements WebAppLinkBuilder
{
	private WebAppPathResolver pathResolver;
	private HttpServletRequest request;
	private HttpServletResponse response;

	private String contextPath;

	@PostConstruct
	public void validateProperties() {
		Assert.notNull( pathResolver );
		Assert.notNull( request );
		Assert.notNull( response );
	}

	@Override
	public String buildLink( String baseLink ) {
		return buildLink( baseLink, true );
	}

	@Override
	public String buildLink( String baseLink, boolean encodeUrl ) {
		boolean shouldEncode = encodeUrl;
		StringBuilder link = new StringBuilder( baseLink == null ? "" : pathResolver.path( baseLink ) );

		if ( link.length() > 0 ) {
			if ( isServerRelative( link ) ) {
				link.delete( 0, 1 );
			}
			else if ( isContextRelative( link ) ) {
				link.insert( 0, contextPath );
			}
		}

		if ( shouldEncode ) {
			shouldEncode = supportsEncoding( link );
		}

		return shouldEncode ? encodeUrl( link ) : link.toString();
	}

	private boolean isServerRelative( CharSequence link ) {
		// starts with ~/ ?
		return link.length() >= 2 && link.charAt( 0 ) == '~' && link.charAt( 1 ) == '/';
	}

	private boolean isContextRelative( final CharSequence link ) {
		// starts with / but not //
		return link.charAt( 0 ) == '/' && ( link.length() == 1 || link.charAt( 1 ) != '/' );
	}

	private boolean supportsEncoding( CharSequence link ) {
		int len = link.length();

		if ( len < 1 ) {
			return true;
		}

		char ch = link.charAt( 0 );

		if ( ch == '#' ) {
			return false;
		}
		else if ( ( ch == 'm' || ch == 'M' ) &&
				len >= 7 &&
				Character.toLowerCase( link.charAt( 1 ) ) == 'a' &&
				Character.toLowerCase( link.charAt( 2 ) ) == 'i' &&
				Character.toLowerCase( link.charAt( 3 ) ) == 'l' &&
				Character.toLowerCase( link.charAt( 4 ) ) == 't' &&
				Character.toLowerCase( link.charAt( 5 ) ) == 'o' &&
				Character.toLowerCase( link.charAt( 6 ) ) == ':' ) {
			return false;
		}

		return true;
	}

	private String encodeUrl( CharSequence link ) {
		return response.encodeURL( link.toString() );
	}

	@Autowired
	public void setPathResolver( WebAppPathResolver pathResolver ) {
		this.pathResolver = pathResolver;
	}

	@Autowired
	public void setRequest( HttpServletRequest request ) {
		Assert.notNull( request );
		this.request = request;

		contextPath = StringUtils.defaultString( request.getContextPath() );
		if ( "/".equals( contextPath ) ) {
			contextPath = "";
		}
	}

	@Autowired
	public void setResponse( HttpServletResponse response ) {
		this.response = response;
	}
}
