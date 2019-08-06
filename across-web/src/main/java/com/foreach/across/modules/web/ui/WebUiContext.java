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

import org.springframework.context.MessageSource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public interface WebUiContext<T>
{
	T getUser();

	void setUser( T user );

	HttpServletRequest getRequest();

	void setRequest( HttpServletRequest request );

	HttpServletResponse getResponse();

	void setResponse( HttpServletResponse response );

	MessageSource getMessageSource();

	void setMessageSource( MessageSource messageSource );

	/**
	 * @return The translated message in the given context (key, locale etc.)
	 */
	String getMessage( String messageKey, Object... parameters );

	void addAttribute( String attributeName, Object attributeValue );

	boolean containsAttribute( String attributeName );

	@SuppressWarnings("unchecked")
	<T> T getAttribute( String attributeName );

	String getParameter( String parameterName );

	String[] getParameterValues( String parameterName );

	Cookie getCookie( String cookieName );

	String getCookieValue( String cookieName );

	void setCookie( String cookieName, String cookieValue, int maxAge );
}