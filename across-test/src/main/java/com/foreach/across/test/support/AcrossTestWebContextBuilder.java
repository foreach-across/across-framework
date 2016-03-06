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
package com.foreach.across.test.support;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ModuleDependencyResolver;
import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.across.test.AcrossTestWebContext;
import com.foreach.across.test.MockAcrossServletContext;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Builder for creating an {@link AcrossTestWebContext}.
 * This builder allows easy configuration of properties and modules to add to an {@link AcrossTestWebContext}.
 * Configures a parent {@link org.springframework.web.context.WebApplicationContext} and adds a {@link MockAcrossServletContext}.
 * Note that {@link com.foreach.across.modules.web.AcrossWebModule} is <strong>not</strong> automatically added to the
 * created context.
 * <p>
 * Once {@link #build()} has been called, the {@link AcrossTestWebContext} will be created and the internal
 * {@link com.foreach.across.core.AcrossContext} bootstrapped.
 *
 * @author Arne Vandamme
 * @since 1.1.2
 */
public class AcrossTestWebContextBuilder extends AcrossTestContextBuilder
{
	private boolean dynamicServletContext = true;

	/**
	 * Should the configured {@link MockAcrossServletContext} allow for dynamic registration of servlets
	 * and filters.  Default is {@code true}.
	 *
	 * @param dynamicServletContext true if dynamic registration should be allowed
	 * @return self
	 */
	public AcrossTestWebContextBuilder dynamicServletContext( boolean dynamicServletContext ) {
		this.dynamicServletContext = dynamicServletContext;
		return this;
	}

	@Override
	public AcrossTestWebContextBuilder configurer( AcrossContextConfigurer... configurer ) {
		return (AcrossTestWebContextBuilder) super.configurer( configurer );
	}

	@Override
	public AcrossTestWebContextBuilder property( String key, Object value ) {
		return (AcrossTestWebContextBuilder) super.property( key, value );
	}

	@Override
	public AcrossTestWebContextBuilder properties( Properties properties ) {
		return (AcrossTestWebContextBuilder) super.properties( properties );
	}

	@Override
	public AcrossTestWebContextBuilder properties( Map<String, Object> properties ) {
		return (AcrossTestWebContextBuilder) super.properties( properties );
	}

	@Override
	public AcrossTestWebContextBuilder properties( PropertySource propertySource ) {
		return (AcrossTestWebContextBuilder) super.properties( propertySource );
	}

	@Override
	public AcrossTestWebContextBuilder dropFirst( boolean dropFirst ) {
		return (AcrossTestWebContextBuilder) super.dropFirst( dropFirst );
	}

	@Override
	public AcrossTestWebContextBuilder useTestDataSource( boolean useTestDataSource ) {
		return (AcrossTestWebContextBuilder) super.useTestDataSource( useTestDataSource );
	}

	@Override
	public AcrossTestWebContextBuilder register( Class<?>... annotatedClasses ) {
		return (AcrossTestWebContextBuilder) super.register( annotatedClasses );
	}

	@Override
	public AcrossTestWebContextBuilder dataSource( DataSource dataSource ) {
		return (AcrossTestWebContextBuilder) super.dataSource( dataSource );
	}

	@Override
	public AcrossTestWebContextBuilder installerDataSource( DataSource installerDataSource ) {
		return (AcrossTestWebContextBuilder) super.installerDataSource( installerDataSource );
	}

	@Override
	public AcrossTestWebContextBuilder modules( String... moduleNames ) {
		return (AcrossTestWebContextBuilder) super.modules( moduleNames );
	}

	@Override
	public AcrossTestWebContextBuilder modules( AcrossModule... modules ) {
		return (AcrossTestWebContextBuilder) super.modules( modules );
	}

	@Override
	public AcrossTestWebContextBuilder moduleDependencyResolver( ModuleDependencyResolver moduleDependencyResolver ) {
		return (AcrossTestWebContextBuilder) super.moduleDependencyResolver( moduleDependencyResolver );
	}

	@Override
	public AcrossTestWebContextBuilder additionalModulePackages( String... packageNames ) {
		return (AcrossTestWebContextBuilder) super.additionalModulePackages( packageNames );
	}

	@Override
	public AcrossTestWebContextBuilder additionalModulePackageClasses( Class<?>... classes ) {
		return (AcrossTestWebContextBuilder) super.additionalModulePackageClasses( classes );
	}

	@Override
	public AcrossTestWebContextBuilder moduleConfigurationPackages( String... packageNames ) {
		return (AcrossTestWebContextBuilder) super.moduleConfigurationPackages( packageNames );
	}

	@Override
	public AcrossTestWebContextBuilder moduleConfigurationPackageClasses( Class<?>... classes ) {
		return (AcrossTestWebContextBuilder) super.moduleConfigurationPackageClasses( classes );
	}

	@Override
	public AcrossTestWebContextBuilder developmentMode( boolean developmentMode ) {
		return (AcrossTestWebContextBuilder) super.developmentMode( developmentMode );
	}

	@Override
	protected AcrossConfigurableApplicationContext createDefaultApplicationContext() {
		AcrossWebApplicationContext wac = new AcrossWebApplicationContext();

		MockAcrossServletContext servletContext = new MockAcrossServletContext();
		servletContext.setDynamicRegistrationAllowed( dynamicServletContext );
		wac.setServletContext( servletContext );

		return wac;
	}

	@Override
	public AcrossTestWebContext build() {
		return (AcrossTestWebContext) super.build();
	}

	@Override
	protected AcrossTestWebContext createTestContext( AcrossConfigurableApplicationContext applicationContext,
	                                                  AcrossContext context ) {
		AcrossWebApplicationContext wac = (AcrossWebApplicationContext) applicationContext;
		wac.getServletContext().setAttribute(
				WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				AcrossContextUtils.getApplicationContext( context )
		);
		return new RunningAcrossContext( wac, context );
	}

	private static class RunningAcrossContext extends AcrossTestWebContext
	{
		RunningAcrossContext( AcrossWebApplicationContext parentApplicationContext,
		                      AcrossContext acrossContext ) {
			setApplicationContext( parentApplicationContext );
			setServletContext( (MockAcrossServletContext) parentApplicationContext.getServletContext() );
			setAcrossContext( acrossContext );

			// Ensure the servlets and filters are initialized
			getServletContext().initialize();
		}
	}
}
