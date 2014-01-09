package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossBootstrapApplicationContextHandler;
import com.foreach.across.core.AcrossModule;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class AcrossBootstrapWebApplicationContextHandler extends AcrossBootstrapApplicationContextHandler
{
	@Override
	public ConfigurableApplicationContext createModuleApplicationContext( ConfigurableApplicationContext parent,
	                                                                      AcrossModule module ) {

		if ( parent instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parent;
			AnnotationConfigWebApplicationContext child = new AnnotationConfigWebApplicationContext();

			child.setServletContext( parentWebContext.getServletContext() );
			child.setParent( parent );
			child.setEnvironment( parent.getEnvironment() );
			child.scan( module.getComponentScanPackages() );

			return child;
		}

		return super.createModuleApplicationContext( parent, module );
	}
}
