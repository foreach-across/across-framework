package com.foreach.across.modules.web.servlet;

import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Initializes a single dispatchers servlet with a root Application context that is initialized upon creation.
 * This allows any modules in the root Application context to extend the ServletContext.
 * <p/>
 * Servlet 3 environments only need to extend this initializer and configure the application context with
 * one or more configuration classes or locations.  Any AcrossContext bootstrapped in the ApplicationContext
 * will be able to extend the ServletContext.
 *
 * @author Arne Vandamme
 * @see
 */
public abstract class AbstractAcrossServletInitializer extends AbstractDispatcherServletInitializer
{
	public static final String DYNAMIC_INITIALIZER = "com.foreach.across.modules.web.servlet.AcrossServletInitializer";

	@Override
	public void onStartup( ServletContext servletContext ) throws ServletException {
		super.onStartup( servletContext );

		extendServletContext( servletContext );
	}

	@Override
	protected void registerContextLoaderListener( ServletContext servletContext ) {
		WebApplicationContext rootAppContext = createRootApplicationContext();
		if ( rootAppContext != null ) {
			servletContext.setAttribute( DYNAMIC_INITIALIZER, this );

			ContextLoaderListener listener = new ShutdownOnlyContextLoaderListener( rootAppContext );
			listener.initWebApplicationContext( servletContext );
			servletContext.removeAttribute( DYNAMIC_INITIALIZER );

			servletContext.addListener( listener );
		}
		else {
			logger.debug( "No ContextLoaderListener registered, as " +
					              "createRootApplicationContext() did not return an application context" );
		}
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AcrossWebApplicationContext context = new AcrossWebApplicationContext();
		configure( context );

		return context;
	}

	/**
	 * Configure the ApplicationContext before it is initialized.
	 *
	 * @param applicationContext AcrossWebApplicationContext that contains the main Across context configuration.
	 */
	protected abstract void configure( AcrossWebApplicationContext applicationContext );

	/**
	 * Creates a default empty WebApplicationContext for the servlet itself.
	 */
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		return new AcrossWebApplicationContext();
	}

	@Override
	protected String[] getServletMappings() {
		// All paths are mapped to this dispatcher servlet
		return new String[] { "/" };
	}

	/**
	 * Extension point for extending the ServletContext during initialization.
	 *
	 * @param servletContext ServletContext while initialization is busy.
	 */
	protected void extendServletContext( ServletContext servletContext ) {
	}
}
