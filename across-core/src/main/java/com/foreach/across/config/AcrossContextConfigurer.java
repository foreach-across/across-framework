package com.foreach.across.config;

import com.foreach.across.core.AcrossContext;

/**
 * Interface to implement when delegate configuration of an AcrossContext.
 *
 * @see AcrossContextConfiguration
 * @see com.foreach.across.config.EnableAcrossContext
 */
public interface AcrossContextConfigurer
{
	void configure( AcrossContext context );
}
