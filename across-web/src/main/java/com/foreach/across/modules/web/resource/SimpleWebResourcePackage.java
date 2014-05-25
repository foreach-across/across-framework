package com.foreach.across.modules.web.resource;

import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 * Groups a number of WebResources together.
 */
public class SimpleWebResourcePackage implements WebResourcePackage
{
	private Collection<WebResource> webResources = Collections.emptyList();

	protected SimpleWebResourcePackage() {
	}

	public SimpleWebResourcePackage( Collection<WebResource> webResources ) {
		setWebResources( webResources );
	}

	protected void setWebResources( Collection<WebResource> webResources ) {
		Assert.notNull( webResources );
		Assert.notEmpty( webResources );
		this.webResources = webResources;
	}

	@Override
	public void install( WebResourceRegistry registry ) {
		for ( WebResource webResource : webResources ) {
			registry.add( webResource );
		}
	}

	@Override
	public void uninstall( WebResourceRegistry registry ) {
		for ( WebResource webResource : webResources ) {
			registry.removeResource( webResource.getType(), webResource.getData() );
		}
	}
}
