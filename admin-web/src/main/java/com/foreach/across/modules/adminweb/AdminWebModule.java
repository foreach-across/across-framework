package com.foreach.across.modules.adminweb;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.adminweb.config.HibernateSessionFactoryConfig;
import com.foreach.across.modules.adminweb.installers.AdminWebSchemaInstaller;

import java.util.Set;

@AcrossDepends(optional = "AcrossWebModule")
public class AdminWebModule extends AcrossModule
{
	@Override
	public String getName() {
		return "AdminWebModule";
	}

	@Override
	public String getDescription() {
		return "Provides user authentication and authorization features with management web pages.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( HibernateSessionFactoryConfig.class ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
			new AdminWebSchemaInstaller()
		};
	}
}
