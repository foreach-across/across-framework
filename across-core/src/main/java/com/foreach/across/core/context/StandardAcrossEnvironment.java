package com.foreach.across.core.context;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

/**
 * Merge semantics for an Across environment are different: the standard property sources
 * are kept in the order of the parent environment.
 *
 * @author Arne Vandamme
 */
public class StandardAcrossEnvironment extends StandardEnvironment
{
	@Override
	public void merge( ConfigurableEnvironment parent ) {
		removeIfParentContains( parent, SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME );
		removeIfParentContains( parent, SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME );

		super.merge( parent );
	}

	private void removeIfParentContains( ConfigurableEnvironment parent, String sourceName ) {
		if ( parent.getPropertySources().contains( sourceName ) ) {
			getPropertySources().remove( sourceName );
		}
	}
}
