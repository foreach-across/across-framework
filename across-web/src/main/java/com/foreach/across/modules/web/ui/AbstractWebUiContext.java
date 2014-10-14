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

package com.foreach.across.modules.web.ui;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Abstract implementation of a standard WebUiContext.  Wrapping around a request/response pair
 * with a link to the requesting user principal.
 *
 * @param <T> Implementation for the user making the request.
 */
public abstract class AbstractWebUiContext<T> implements WebUiContext<T>
{
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected MessageSource messageSource;
	protected T user;

	protected AbstractWebUiContext( HttpServletRequest request, HttpServletResponse response ) {
		this( request, response, null );
	}

	protected AbstractWebUiContext( HttpServletRequest request, HttpServletResponse response, T user ) {
		this( request, response, user, null );
	}

	protected AbstractWebUiContext( HttpServletRequest request,
	                                HttpServletResponse response,
	                                T user,
	                                MessageSource messageSource ) {
		this.request = request;
		this.response = response;
		this.messageSource = messageSource;
		this.user = user;
	}

	public T getUser() {
		return user;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setRequest( HttpServletRequest request ) {
		this.request = request;
	}

	public void setResponse( HttpServletResponse response ) {
		this.response = response;
	}

	public void setMessageSource( MessageSource messageSource ) {
		this.messageSource = messageSource;
	}

	public void setUser( T user ) {
		this.user = user;
	}

	/**
	 * @return The translated message in the given context (key, locale etc.)
	 */
	public String getMessage( String messageKey, Object... parameters ) {
		return messageSource.getMessage( messageKey, parameters, messageKey, Locale.getDefault() );
	}

	public void addAttribute( String attributeName, Object attributeValue ) {
		request.setAttribute( attributeName, attributeValue );
	}

	public boolean containsAttribute( String attributeName ) {
		return request.getAttribute( attributeName ) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute( String attributeName ) {
		return (T) request.getAttribute( attributeName );
	}

	public String getParameter( String parameterName ) {
		return request.getParameter( parameterName );
	}

	public String[] getParameterValues( String parameterName ) {
		return request.getParameterValues( parameterName );
	}

	public Cookie getCookie( String cookieName ) {
		if ( request.getCookies() != null ) {
			for ( Cookie c : request.getCookies() ) {
				if ( StringUtils.equals( c.getName(), cookieName ) ) {
					return c;
				}
			}
		}

		return null;
	}

	public String getCookieValue( String cookieName ) {
		if ( request.getCookies() != null ) {
			for ( Cookie lu : request.getCookies() ) {
				if ( StringUtils.equals( lu.getName(), cookieName ) ) {
					return lu.getValue();
				}
			}
		}
		return null;
	}

	public void setCookie( String cookieName, String cookieValue, int maxAge ) {
		Cookie lu = new Cookie( cookieName, cookieValue );
		lu.setMaxAge( maxAge );
		lu.setPath( "/" );
		response.addCookie( lu );
	}
}
