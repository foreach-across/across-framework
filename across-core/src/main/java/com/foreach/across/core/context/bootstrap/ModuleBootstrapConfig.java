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

package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.installers.InstallerSettings;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;

import java.util.*;

/**
 * Represents the actual bootstrap configuration of an AcrossModule.
 * This is the configuration that can be modified during the bootstrap process,
 * without changing the initial AcrossModule configuration.
 */
public class ModuleBootstrapConfig
{
	private final int bootstrapIndex;
	private final AcrossModule module;

	private BeanFilter exposeFilter;
	private ExposedBeanDefinitionTransformer exposeTransformer;
	private Set<ApplicationContextConfigurer> applicationContextConfigurers = new LinkedHashSet<>();
	private Collection<Object> installers = new LinkedList<>();
	private InstallerSettings installerSettings;

	public ModuleBootstrapConfig( AcrossModule module, int bootstrapIndex ) {
		this.module = module;
		this.bootstrapIndex = bootstrapIndex;
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

	public int getBootstrapIndex() {
		return bootstrapIndex;
	}

	/**
	 * Adds filters that will be used after module bootstrap to copy beans to the parent context.
	 * This adds the filters to the already configured expose filter.
	 *
	 * @param exposeFilters One or more filters that beans should match to be exposed to other modules.
	 */
	public void addExposeFilter( BeanFilter... exposeFilters ) {
		BeanFilter[] members = exposeFilters;
		BeanFilter current = getExposeFilter();

		if ( current != null ) {
			members = new BeanFilter[members.length + 1];
			members[0] = current;
			System.arraycopy( exposeFilters, 0, members, 1, exposeFilters.length );
		}

		BeanFilterComposite composite = new BeanFilterComposite( members );
		setExposeFilter( composite );
	}

	public ExposedBeanDefinitionTransformer getExposeTransformer() {
		return exposeTransformer;
	}

	public void setExposeTransformer( ExposedBeanDefinitionTransformer exposeTransformer ) {
		this.exposeTransformer = exposeTransformer;
	}

	public Set<ApplicationContextConfigurer> getApplicationContextConfigurers() {
		return applicationContextConfigurers;
	}

	public void setApplicationContextConfigurers( Set<ApplicationContextConfigurer> applicationContextConfigurers ) {
		this.applicationContextConfigurers = applicationContextConfigurers;
	}

	public void addApplicationContextConfigurer( Class<?>... annotatedClasses ) {
		addApplicationContextConfigurer( new AnnotatedClassConfigurer( annotatedClasses ) );
	}

	public void addApplicationContextConfigurer( ApplicationContextConfigurer... configurers ) {
		addApplicationContextConfigurers( Arrays.asList( configurers ) );
	}

	public void addApplicationContextConfigurers( Collection<ApplicationContextConfigurer> configurers ) {
		applicationContextConfigurers.addAll( configurers );
	}

	public InstallerSettings getInstallerSettings() {
		return installerSettings;
	}

	public void setInstallerSettings( InstallerSettings installerSettings ) {
		this.installerSettings = installerSettings;
	}

	public Collection<Object> getInstallers() {
		return installers;
	}

	public void setInstallers( Collection<Object> installers ) {
		this.installers = installers;
	}
}
