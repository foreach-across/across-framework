package com.foreach.across.modules.web.ui;

import org.springframework.context.MessageSource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
