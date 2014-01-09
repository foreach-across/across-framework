package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossBootstrapApplicationContextHandler;
import com.foreach.across.core.AcrossContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Extends the standard AcrossContext by supporting Spring WebApplicationContext.
 */
public class AcrossWebContext extends AcrossContext
{
	public AcrossWebContext() {
		super();
	}

	public AcrossWebContext( ApplicationContext parentContext ) {
		super( parentContext );
	}

	@Override
	protected ConfigurableApplicationContext createApplicationContext( ApplicationContext parent ) {
		if ( parent == null || parent instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parent;
			AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();

			if ( parent != null ) {
				applicationContext.setParent( parent );
				applicationContext.setServletContext( parentWebContext.getServletContext() );

				if ( parent.getEnvironment() instanceof ConfigurableEnvironment ) {
					applicationContext.setEnvironment( (ConfigurableEnvironment) parent.getEnvironment() );
				}
			}

			return applicationContext;
		}
		else {
			return super.createApplicationContext( parent );
		}
	}

	@Override
	protected AcrossBootstrapApplicationContextHandler createBootstrapHandler() {
		return new AcrossBootstrapWebApplicationContextHandler();
	}
}
