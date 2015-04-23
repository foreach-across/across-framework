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
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapper;
import com.foreach.across.core.context.bootstrap.BootstrapAdapter;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.web.config.*;
import com.foreach.across.modules.web.config.multipart.MultipartResolverConfiguration;
import com.foreach.across.modules.web.context.WebBootstrapApplicationContextFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

public class AcrossWebModule extends AcrossModule implements BootstrapAdapter
{
	public static final String NAME = "AcrossWebModule";

	/**
	 * Name of the {@link org.springframework.format.support.FormattingConversionService} bean that web will
	 * used by default.
	 */
	public static final String CONVERSION_SERVICE_BEAN = "mvcConversionService";

	// AcrossWebModule is the special case providing root resources
	public static final String RESOURCES = "";

	private String viewsResourcePath;
	private AcrossWebViewSupport[] supportedViews =
			new AcrossWebViewSupport[] { AcrossWebViewSupport.JSP, AcrossWebViewSupport.THYMELEAF };

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
				new AnnotatedClassConfigurer(
						AcrossWebConfig.class,
						AcrossWebTemplateConfig.class,
						AcrossWebDefaultMvcConfiguration.class,
						ConversionServiceExposingInterceptorConfiguration.class,
						MultipartResolverConfiguration.class
				)
		);
		contextConfigurers.add(
				new ComponentScanConfigurer( "com.foreach.across.modules.web.menu" )
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
