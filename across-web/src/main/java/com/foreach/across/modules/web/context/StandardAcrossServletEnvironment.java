package com.foreach.across.modules.web.context;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Servlet version of an Across environment.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.core.context.StandardAcrossEnvironment
 */
public class StandardAcrossServletEnvironment extends StandardServletEnvironment
{
	@Override
	public void merge( ConfigurableEnvironment parent ) {
		removeIfParentContains( parent, JNDI_PROPERTY_SOURCE_NAME );
		removeIfParentContains( parent, SERVLET_CONFIG_PROPERTY_SOURCE_NAME );
		removeIfParentContains( parent, SERVLET_CONTEXT_PROPERTY_SOURCE_NAME );

		super.merge( parent );
	}

	private void removeIfParentContains( ConfigurableEnvironment parent, String sourceName ) {
		if ( parent.getPropertySources().contains( sourceName ) ) {
			getPropertySources().remove( sourceName );
		}
	}
}
