package com.foreach.across.modules.web.servlet;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;

/**
 * Only supports the context destroyed ServletContextEvent, as it assumes
 * that the context has already been initialized when passed to the listener.
 *
 * @author Arne Vandamme
 */
class ShutdownOnlyContextLoaderListener extends ContextLoaderListener
{
	public ShutdownOnlyContextLoaderListener( WebApplicationContext context ) {
		super( context );
	}

	@Override
	public void contextInitialized( ServletContextEvent event ) {
		// do nothing
	}
}
