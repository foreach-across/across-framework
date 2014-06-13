package com.foreach.across.modules.adminweb.resource;

import com.foreach.across.modules.web.resource.SimpleWebResourcePackage;
import com.foreach.across.modules.web.resource.WebResource;

import java.util.Arrays;

public class JQueryWebResourcePackage extends SimpleWebResourcePackage
{
	public static final String NAME = "jquery";

	public JQueryWebResourcePackage( boolean minified ) {
		this( minified, "1.11.0" );
	}

	public JQueryWebResourcePackage( boolean minified, String version ) {
		if ( minified ) {
			setWebResources( Arrays.asList( new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
			                                                 "//ajax.googleapis.com/ajax/libs/jquery/" + version + "/jquery.min.js",
			                                                 WebResource.EXTERNAL )
			                 )
			);
		}
		else {
			setWebResources( Arrays.asList( new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
			                                                 "//ajax.googleapis.com/ajax/libs/jquery/" + version + "/jquery.js",
			                                                 WebResource.EXTERNAL )
			                 )
			);
		}
	}
}
