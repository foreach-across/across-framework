package com.foreach.across.modules.web.servlet;

import com.foreach.across.core.AcrossException;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Base configurer class that can be used to dynamically extend a ServletContext.
 * Works by checking if the {@link com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer}
 * was used to bootstrap the context.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer
 */
public abstract class AcrossWebDynamicServletConfigurer implements ServletContextAware
{
	@Override
	public void setServletContext( ServletContext servletContext ) {
		Object loader = servletContext.getAttribute( AbstractAcrossServletInitializer.DYNAMIC_INITIALIZER );

		try {
			if ( loader instanceof AbstractAcrossServletInitializer ) {
				dynamicConfigurationAllowed( servletContext );
			}
			else {
				dynamicConfigurationDenied( servletContext );
			}
		}
		catch ( ServletException se ) {
			throw new AcrossException( se );
		}
	}

	/**
	 * Called when the ServletContext initialization is still busy and dynamic extension should be possible.
	 *
	 * @param servletContext ServletContext that is being initialized.
	 */
	protected abstract void dynamicConfigurationAllowed( ServletContext servletContext ) throws ServletException;

	/**
	 * Called when the ServletContext does not allow dynamic extension.  Probably because
	 * the ApplicationContext was not started using an {@link com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer}.
	 * <p/>
	 * Usually means this configurer can not do its part and some feedback should be presented to the user.
	 *
	 * @param servletContext ServletContext that already is initialized.
	 */
	protected abstract void dynamicConfigurationDenied( ServletContext servletContext ) throws ServletException;
}
