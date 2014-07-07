package com.foreach.across.core.context.beans;

import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * Helper class that wraps around a singleton to register it as primary bean under that name.
 */
public class PrimarySingletonBean extends SingletonBean
{
	public PrimarySingletonBean( Object object ) {
		this( object, new AutowireCandidateQualifier[0] );
	}

	public PrimarySingletonBean( Object object, AutowireCandidateQualifier... qualifiers ) {
		super( object );

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setPrimary( true );

		for ( AutowireCandidateQualifier qualifier : qualifiers ) {
			definition.addQualifier( qualifier );
		}

		setBeanDefinition( definition );
	}
}
