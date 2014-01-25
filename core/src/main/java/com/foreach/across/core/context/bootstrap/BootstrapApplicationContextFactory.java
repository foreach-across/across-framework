package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * In charge of creating the ApplicationContext and loading all the beans.
 */
public interface BootstrapApplicationContextFactory
{
	/**
	 * Create the Spring ApplicationContext for the root of the AcrossContext.
	 * Optionally a parent ApplicationContext can be
	 *
	 * @param across                   AcrossContext being created.
	 * @param parentApplicationContext Parent ApplicationContext, can be null.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                     ApplicationContext parentApplicationContext );

	/**
	 * Create the Spring ApplicationContext for a particular AcrossModule.
	 *
	 * @param across        AcrossContext being loaded.
	 * @param module        AcrossModule being created.
	 * @param parentContext Contains the parent context.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                     AcrossModule module,
	                                                     AcrossApplicationContext parentContext );

	/**
	 * Loads beans and definitions in the ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param module  AcrossModule being loaded.
	 * @param context Contains the Spring ApplicationContext for the module.
	 */
	void loadApplicationContext( AcrossContext across, AcrossModule module, AcrossApplicationContext context );
}
