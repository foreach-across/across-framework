package com.foreach.across.modules.spring.security;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.filters.ClassBeanFilter;
import com.foreach.across.core.filters.NamedBeanFilter;
import com.foreach.across.modules.spring.security.config.GlobalWebSecurityConfiguration;
import com.foreach.across.modules.spring.security.config.ModuleGlobalMethodSecurityConfiguration;
import com.foreach.across.modules.spring.security.config.SpringSecurityThymeleafConfiguration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

import java.util.Map;
import java.util.Set;

@AcrossRole(AcrossModuleRole.POSTPROCESSOR)
public class SpringSecurityModule extends AcrossModule
{
	public static final String NAME = "SpringSecurityModule";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Hooks up Spring Security and allows WebSecurityConfigurers to be defined in separate modules.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( GlobalWebSecurityConfiguration.class,
		                                                      SpringSecurityThymeleafConfiguration.class ) );
	}

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule,
	                                 Map<AcrossModule, ModuleBootstrapConfig> modulesInOrder ) {
		currentModule.setExposeFilter( new BeanFilterComposite(
				new ClassBeanFilter( FilterChainProxy.class, WebInvocationPrivilegeEvaluator.class,
				                     SecurityExpressionHandler.class ),
				new NamedBeanFilter( "requestDataValueProcessor" ) ) );

		for ( ModuleBootstrapConfig moduleBootstrapConfig : modulesInOrder.values() ) {
			if ( moduleBootstrapConfig != currentModule ) {
				moduleBootstrapConfig.addApplicationContextConfigurer(
						new AnnotatedClassConfigurer( ModuleGlobalMethodSecurityConfiguration.class ) );
			}
		}
	}
}
