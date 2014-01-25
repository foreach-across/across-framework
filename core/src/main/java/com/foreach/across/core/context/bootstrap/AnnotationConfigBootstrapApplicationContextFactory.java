package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class AnnotationConfigBootstrapApplicationContextFactory implements BootstrapApplicationContextFactory
{
	/**
	 * Create the Spring ApplicationContext for the root of the AcrossContext.
	 * Optionally a parent ApplicationContext can be
	 *
	 * @param across                   AcrossContext being created.
	 * @param parentApplicationContext Parent ApplicationContext, can be null.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ApplicationContext parentApplicationContext ) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

		if ( parentApplicationContext != null ) {
			applicationContext.setParent( parentApplicationContext );

			if ( parentApplicationContext.getEnvironment() instanceof ConfigurableEnvironment ) {
				applicationContext.setEnvironment(
						(ConfigurableEnvironment) parentApplicationContext.getEnvironment() );
			}
		}

		return applicationContext;
	}

	/**
	 * Create the Spring ApplicationContext.
	 *
	 * @param across        AcrossContext being loaded.
	 * @param module        AcrossModule being loaded.
	 * @param parentContext Contains the parent context.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            AcrossModule module,
	                                                            AcrossApplicationContext parentContext ) {
		AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
		child.setParent( parentContext.getApplicationContext() );
		child.setEnvironment( parentContext.getApplicationContext().getEnvironment() );

		return child;
	}

	/**
	 * Loads beans and definitions in the ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param module  AcrossModule being loaded.
	 * @param context Contains the Spring ApplicationContext for the module.
	 */
	public void loadApplicationContext( AcrossContext across, AcrossModule module, AcrossApplicationContext context ) {
		AnnotationConfigApplicationContext child = (AnnotationConfigApplicationContext) context.getApplicationContext();

		child.scan( module.getComponentScanPackages() );

		child.refresh();
		child.start();
	}
}
