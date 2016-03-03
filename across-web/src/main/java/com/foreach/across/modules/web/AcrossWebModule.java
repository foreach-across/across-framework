/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.web;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.web.context.WebBootstrapApplicationContextFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

public class AcrossWebModule extends AcrossModule implements BootstrapAdapter
{
	public static final String DEFAULT_VIEWS_RESOURCES_PATH = "/across/resources";

	public static final String NAME = "AcrossWebModule";

	/**
	 * Name of the {@link org.springframework.format.support.FormattingConversionService} bean that web will
	 * used by default.
	 */
	public static final String CONVERSION_SERVICE_BEAN = "mvcConversionService";

	// AcrossWebModule is the special case providing root resources
	public static final String RESOURCES = "";

	/**
	 * Set the base url path that will be used to access views.
	 *
	 * @param viewsResourcePath Url path prefix for views resources.
	 * @deprecated set via properties instead
	 */
	@Deprecated
	public void setViewsResourcePath( String viewsResourcePath ) {
		setProperty( AcrossWebModuleSettings.VIEWS_RESOURCES_PATH, viewsResourcePath );
	}

	/**
	 * Configure the view resolvers that should be created.
	 * By default both JSP and Thymeleaf are created.
	 *
	 * @param viewSupport View engine that should be configured.
	 * @deprecated set via properties instead
	 */
	@Deprecated
	public void setSupportViews( AcrossWebViewSupport... viewSupport ) {
		setProperty( "acrossWebModule.views.thymeleaf.enabled",
		             ArrayUtils.contains( viewSupport, AcrossWebViewSupport.THYMELEAF ) );
		setProperty( "acrossWebModule.views.jsp.enabled",
		             ArrayUtils.contains( viewSupport, AcrossWebViewSupport.JSP ) );
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getResourcesKey() {
		return RESOURCES;
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
		contextConfigurers.add(
				new ComponentScanConfigurer(
						getClass().getPackage().getName() + ".config",
						getClass().getPackage().getName() + ".menu"
				)
		);
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
