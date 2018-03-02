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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.web.config.AcrossWebModuleDevSettings;
import com.foreach.across.modules.web.error.AcrossModuleDefaultErrorViewResolver;
import com.foreach.across.modules.web.menu.Menu;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.UriComponentsContributor;

import java.util.Set;

public class AcrossWebModule extends AcrossModule
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

	public AcrossWebModule() {
		expose( RestTemplateBuilder.class, HttpMessageConverters.class, ObjectMapper.class, Jackson2ObjectMapperBuilder.class );
		exposeClass( "com.google.gson.Gson" );
	}

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
		setProperty( "across.web.views.thymeleaf.enabled",
		             ArrayUtils.contains( viewSupport, AcrossWebViewSupport.THYMELEAF ) );
		setProperty( "across.web.views.jsp.enabled",
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
		return "Sets up module-based Spring MVC support.  Allows other modules to provide Spring MVC configuration.";
	}

	/**
	 * Register the default ApplicationContextConfigurers for this module.
	 *
	 * @param contextConfigurers Set of existing configurers to add to.
	 */
	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new ComponentScanConfigurer( AcrossWebModuleDevSettings.class, Menu.class ) );
	}

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule, AcrossBootstrapConfig contextConfig ) {
		contextConfig.extendModule( "DynamicApplicationModule", AcrossModuleDefaultErrorViewResolver.class );

		contextConfig.getModule( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE )
		             .expose( Validator.class, UriComponentsContributor.class );
	}
}
