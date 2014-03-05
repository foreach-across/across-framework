package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.BeanDefinitionTransformer;

import java.util.*;

/**
 * Represents the actual bootstrap configuration of an AcrossModule.
 * This is the configuration that can be modified during the bootstrap process,
 * without changing the initial AcrossModule configuration.
 */
public class ModuleBootstrapConfig
{
	private final AcrossModule module;

	private BeanFilter exposeFilter;
	private BeanDefinitionTransformer exposeTransformer;
	private Set<ApplicationContextConfigurer> applicationContextConfigurers =
			new LinkedHashSet<ApplicationContextConfigurer>();
	private Collection<Object> installers = new LinkedList<Object>();

	public ModuleBootstrapConfig( AcrossModule module ) {
		this.module = module;
	}

	public AcrossModule getModule() {
		return module;
	}

	public String getModuleName() {
		return module.getName();
	}

	public BeanFilter getExposeFilter() {
		return exposeFilter;
	}

	public void setExposeFilter( BeanFilter exposeFilter ) {
		this.exposeFilter = exposeFilter;
	}

	public BeanDefinitionTransformer getExposeTransformer() {
		return exposeTransformer;
	}

	public void setExposeTransformer( BeanDefinitionTransformer exposeTransformer ) {
		this.exposeTransformer = exposeTransformer;
	}

	public Set<ApplicationContextConfigurer> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	public void setApplicationContextConfigurers( Set<ApplicationContextConfigurer> applicationContextConfigurers ) {
		this.applicationContextConfigurers = applicationContextConfigurers;
	}

	public void addApplicationContextConfigurer( ApplicationContextConfigurer... configurers ) {
		addApplicationContextConfigurers( Arrays.asList( configurers ) );
	}

	public void addApplicationContextConfigurers( Collection<ApplicationContextConfigurer> configurers ) {
		applicationContextConfigurers.addAll( configurers );
	}

	public Collection<Object> getInstallers() {
		return installers;
	}

	public void setInstallers( Collection<Object> installers ) {
		this.installers = installers;
	}
}
