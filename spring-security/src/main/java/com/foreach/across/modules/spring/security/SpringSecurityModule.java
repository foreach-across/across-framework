package com.foreach.across.modules.spring.security;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.filters.ClassBeanFilter;
import com.foreach.across.core.filters.NamedBeanFilter;
import com.foreach.across.modules.spring.security.config.ModuleGlobalMethodSecurityConfiguration;
import com.foreach.across.modules.spring.security.config.SpringSecurityAcrossWebConfiguration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

import java.util.Set;

@AcrossRole(AcrossModuleRole.POSTPROCESSOR)
public class SpringSecurityModule extends AcrossModule
{
	public static final String NAME = "SpringSecurityModule";

	public SpringSecurityModule() {
		setExposeFilter(
				new BeanFilterComposite(
						new ClassBeanFilter(
								FilterChainProxy.class,
								WebInvocationPrivilegeEvaluator.class,
								SecurityExpressionHandler.class
						),
						new NamedBeanFilter( "requestDataValueProcessor" )
				)
		);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Hooks up Spring Security.  Requires at least one custom WebSecurityConfigurer class to be added at runtime.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( SpringSecurityAcrossWebConfiguration.class ) );
	}

	@Override
	public void prepareForBootstrap( ModuleBootstrapConfig currentModule, AcrossBootstrapConfig contextConfig ) {
		for ( ModuleBootstrapConfig moduleBootstrapConfig : contextConfig.getModules() ) {
			if ( moduleBootstrapConfig != currentModule ) {
				moduleBootstrapConfig.addApplicationContextConfigurer(
						new AnnotatedClassConfigurer( ModuleGlobalMethodSecurityConfiguration.class )
				);
			}
		}

		// Fallback to regular authentication configuration in case there is no web context
		if ( !contextConfig.hasModule( "AcrossWebModule" ) ) {
			currentModule.addApplicationContextConfigurer(
					new AnnotatedClassConfigurer(
							AuthenticationConfiguration.class,
							ObjectPostProcessorConfiguration.class
					)
			);
		}
	}
}
