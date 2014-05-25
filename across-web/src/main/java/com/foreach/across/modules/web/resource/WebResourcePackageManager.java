package com.foreach.across.modules.web.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows a collection of WebResource instances to be bundled under a single name.
 */
public class WebResourcePackageManager
{
	private final Map<String, WebResourcePackage> packages = new HashMap<>();

	public void register( String name, WebResourcePackage webResourcePackage ) {
		packages.put( name, webResourcePackage );
	}

	public void unregister( String name ) {
		packages.remove( name );
	}

	public WebResourcePackage getPackage( String name ) {
		return packages.get( name );
	}
}
