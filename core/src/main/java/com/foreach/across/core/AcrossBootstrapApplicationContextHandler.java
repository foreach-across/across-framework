package com.foreach.across.core;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AcrossBootstrapApplicationContextHandler
{
	public ConfigurableApplicationContext createModuleApplicationContext( ConfigurableApplicationContext parent,
	                                                                      AcrossModule module ) {

		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
		child.setParent( parent );
		child.setEnvironment( parent.getEnvironment() );
		child.scan( module.getComponentScanPackages() );

		return child;
	}
}
