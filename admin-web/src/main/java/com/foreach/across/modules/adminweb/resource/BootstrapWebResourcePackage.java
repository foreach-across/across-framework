package com.foreach.across.modules.adminweb.resource;

import com.foreach.across.modules.web.resource.SimpleWebResourcePackage;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;

import java.util.Arrays;

/**
 * Boostrap css, requires jquery as well.
 */
public class BootstrapWebResourcePackage extends SimpleWebResourcePackage
{
	public static final String NAME = "bootstrap";

	public BootstrapWebResourcePackage( boolean minified ) {
		this( minified, "3.1.1" );
	}

	public BootstrapWebResourcePackage( boolean minified, String version ) {
		if ( minified ) {
			setWebResources( Arrays.asList( new WebResource( WebResource.CSS, NAME,
			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/css/bootstrap.min.css",
			                                                 WebResource.EXTERNAL ),
//			                                new WebResource( WebResource.CSS, NAME + "-theme",
//			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/css/bootstrap-theme.min.css",
//			                                                 WebResource.EXTERNAL ),
			                                new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/js/bootstrap.min.js",
			                                                 WebResource.EXTERNAL )
			                 )
			);
		}
		else {
			setWebResources( Arrays.asList( new WebResource( WebResource.CSS, NAME,
			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/css/bootstrap.css",
			                                                 WebResource.EXTERNAL ),
//			                                new WebResource( WebResource.CSS, NAME + "-theme",
//			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/css/bootstrap-theme.css",
//			                                                 WebResource.EXTERNAL ),
			                                new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME,
			                                                 "//netdna.bootstrapcdn.com/bootstrap/" + version + "/js/bootstrap.js",
			                                                 WebResource.EXTERNAL )
			                 )
			);
		}
	}

	@Override
	public void install( WebResourceRegistry registry ) {
		registry.addPackage( JQueryWebResourcePackage.NAME );

		super.install( registry );
	}
}
