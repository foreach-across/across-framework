package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.web.context.WebBootstrapApplicationContextFactory;

import java.util.Set;

public class AcrossWebModule extends AcrossModule implements BootstrapAdapter
{
	private String viewsResourcePath;

	public String getViewsResourcePath() {
		return viewsResourcePath;
	}

	/**
	 * Set the base url path that will be used to access views.
	 *
	 * @param viewsResourcePath Url path prefix for views resources.
	 */
	public void setViewsResourcePath( String viewsResourcePath ) {
		this.viewsResourcePath = viewsResourcePath;
	}

	@Override
	public String getName() {
		return "AcrossWebModule";
	}

	@Override
	public String getDescription() {
		return "Base Across web functionality based on spring mvc";
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( AcrossWebConfig.class ) );
		contextConfigurers.add( new ComponentScanConfigurer( "com.foreach.across.modules.web.menu",
		                                                     "com.foreach.across.modules.web.ui" ) );
	}

	/**
	 * Customize the AcrossBootstrapper involved.
	 *
	 * @param bootstrapper AcrossBootstrapper instance.
	 */
	public void customizeBootstrapper( AcrossBootstrapper bootstrapper ) {
		bootstrapper.setApplicationContextFactory( new WebBootstrapApplicationContextFactory() );
	}
}
