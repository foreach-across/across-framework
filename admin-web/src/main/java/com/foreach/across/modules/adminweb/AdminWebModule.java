package com.foreach.across.modules.adminweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.adminweb.config.AdminWebMvcConfiguration;
import com.foreach.across.modules.adminweb.config.AdminWebSecurityConfiguration;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.web.AcrossWebModule;

import java.util.Set;

@AcrossDepends(required = { AcrossWebModule.NAME, SpringSecurityModule.NAME })
public class AdminWebModule extends AcrossModule
{
	public static final String NAME = "AdminWebModule";

	private String rootPath = "/admin";

	/**
	 * @return The root path for all AdminWebControllers.
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * Set the root path that all AdminWebController instances should use.  All request mappings
	 * will be prefixed with the path specified here.
	 *
	 * @param rootPath The root path for all AdminWebControllers.
	 * @see org.springframework.web.bind.annotation.RequestMapping
	 */
	public void setRootPath( String rootPath ) {
		this.rootPath = rootPath;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provides a basic administrative web interface with user authentication and authorization.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( AdminWebMvcConfiguration.class/*, AdminWebSecurityConfiguration.class*/ ) );
	}

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 AcrossBootstrapConfig contextConfig ) {
		contextConfig.extendModule( "SpringSecurityModule", AdminWebSecurityConfiguration.class );
	}
}
