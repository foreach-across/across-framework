package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossApplicationContext;
import com.foreach.across.core.context.bootstrap.AnnotationConfigBootstrapApplicationContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class WebBootstrapApplicationContextFactory extends AnnotationConfigBootstrapApplicationContextFactory
{
	/**
	 * Create the Spring ApplicationContext for the root of the AcrossContext.
	 * Optionally a parent ApplicationContext can be
	 *
	 * @param across                   AcrossContext being created.
	 * @param parentApplicationContext Parent ApplicationContext, can be null.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            ApplicationContext parentApplicationContext ) {
		if ( parentApplicationContext == null || parentApplicationContext instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentApplicationContext;
			AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();

			if ( parentApplicationContext != null ) {
				applicationContext.setParent( parentApplicationContext );
				applicationContext.setServletContext( parentWebContext.getServletContext() );

				if ( parentApplicationContext.getEnvironment() instanceof ConfigurableEnvironment ) {
					applicationContext.setEnvironment(
							(ConfigurableEnvironment) parentApplicationContext.getEnvironment() );
				}
			}

			return applicationContext;
		}

		return super.createApplicationContext( across, parentApplicationContext );
	}

	/**
	 * Create the Spring ApplicationContext.
	 *
	 * @param across        AcrossContext being loaded.
	 * @param module        AcrossModule being loaded.
	 * @param parentContext Contains the parent context.
	 * @return Spring ApplicationContext instance implementing AbstractApplicationContext.
	 */
	@Override
	public AbstractApplicationContext createApplicationContext( AcrossContext across,
	                                                            AcrossModule module,
	                                                            AcrossApplicationContext parentContext ) {
		if ( parentContext.getApplicationContext() instanceof WebApplicationContext ) {
			WebApplicationContext parentWebContext = (WebApplicationContext) parentContext.getApplicationContext();
			AnnotationConfigWebApplicationContext child = new AnnotationConfigWebApplicationContext();

			child.setServletContext( parentWebContext.getServletContext() );
			child.setParent( parentContext.getApplicationContext() );
			child.setEnvironment( parentContext.getApplicationContext().getEnvironment() );

			return child;
		}

		return super.createApplicationContext( across, module, parentContext );
	}

	/**
	 * Loads beans and definitions in the ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param module  AcrossModule being loaded.
	 * @param context Contains the Spring ApplicationContext for the module.
	 */
	public void loadApplicationContext( AcrossContext across, AcrossModule module, AcrossApplicationContext context ) {
		AnnotationConfigWebApplicationContext child =
				(AnnotationConfigWebApplicationContext) context.getApplicationContext();

		child.scan( module.getComponentScanPackages() );

		child.refresh();
		child.start();
	}
}
