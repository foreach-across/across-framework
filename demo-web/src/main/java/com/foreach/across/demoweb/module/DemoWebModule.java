package com.foreach.across.demoweb.module;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.adminweb.AdminWebModule;

import java.util.Set;

@AcrossDepends(required = { AdminWebModule.NAME })
public class DemoWebModule extends AcrossModule
{
	@Override
	public String getName() {
		return "DemoWebModule";
	}

	@Override
	public String getDescription() {
		return "Module representing the DemoWeb functionality.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		super.registerDefaultApplicationContextConfigurers( contextConfigurers );

		//contextConfigurers.add( new AnnotatedClassConfigurer( WebMvcSecurityConfiguration.class ) );
	}
}
