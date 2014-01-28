package com.foreach.across.modules.web.resource;

import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;

public class WebResourceUtils
{
	/**
	 * Default key under which the registry is put in the request attributes
	 */
	public static final String REGISTRY_ATTRIBUTE_KEY = "webResourceRegistry";

	protected WebResourceUtils() {
	}

	public static void storeRegistry( WebResourceRegistry registry, HttpServletRequest request ) {
		request.setAttribute( REGISTRY_ATTRIBUTE_KEY, registry );
	}

	public static WebResourceRegistry getRegistry( WebRequest request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY, WebRequest.SCOPE_REQUEST );
	}

	public static WebResourceRegistry getRegistry( HttpServletRequest request ) {
		return (WebResourceRegistry) request.getAttribute( REGISTRY_ATTRIBUTE_KEY );
	}
}
