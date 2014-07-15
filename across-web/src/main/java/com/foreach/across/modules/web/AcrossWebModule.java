package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.web.config.*;
import com.foreach.across.modules.web.context.WebBootstrapApplicationContextFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AcrossWebModule extends AcrossModule implements BootstrapAdapter
{
	public static final String NAME = "AcrossWebModule";

	private boolean developmentMode = false;
	private String viewsResourcePath;
	private AcrossWebViewSupport[] supportedViews =
			new AcrossWebViewSupport[] { AcrossWebViewSupport.JSP, AcrossWebViewSupport.THYMELEAF };
	private Map<String, String> developmentViews = new HashMap<String, String>();

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

	/**
	 * Configure the view resolvers that should be created.
	 * By default both JSP and Thymeleaf are created.
	 *
	 * @param viewSupport View engine that should be configured.
	 */
	public void setSupportViews( AcrossWebViewSupport... viewSupport ) {
		this.supportedViews = viewSupport;
	}

	/**
	 * @return The collection of view resolvers that will be created upon bootstrap.
	 */
	public AcrossWebViewSupport[] getSupportedViews() {
		return supportedViews;
	}

	public boolean isDevelopmentMode() {
		return developmentMode;
	}

	public void setDevelopmentMode( boolean developmentMode ) {
		this.developmentMode = developmentMode;
	}

	public Map<String, String> getDevelopmentViews() {
		return developmentViews;
	}

	public void setDevelopmentViews( Map<String, String> developmentViews ) {
		this.developmentViews = developmentViews;
	}

	public void addDevelopmentViews( String moduleKey, String path ) {
		developmentViews.put( moduleKey, path );
	}

	@Override
	public String getName() {
		return NAME;
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
		contextConfigurers.add( new AnnotatedClassConfigurer( AcrossWebConfig.class, AcrossWebTemplateConfig.class ) );
		contextConfigurers.add( new AnnotatedClassConfigurer( AcrossWebDefaultMvcConfiguration.class ) );
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

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 AcrossBootstrapConfig contextConfig ) {
		if ( ArrayUtils.contains( supportedViews, AcrossWebViewSupport.JSP ) ) {
			currentModule.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer( JstlViewSupportConfiguration.class ) );
		}
		if ( ArrayUtils.contains( supportedViews, AcrossWebViewSupport.THYMELEAF ) ) {
			currentModule.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer( ThymeleafViewSupportConfiguration.class ) );
		}
	}
}
