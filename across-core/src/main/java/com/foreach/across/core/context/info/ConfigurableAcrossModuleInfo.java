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

package com.foreach.across.core.context.info;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.ExposedModuleBeanRegistry;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConfigurableAcrossModuleInfo implements AcrossModuleInfo
{
	private final int index;
	private final AcrossContextInfo context;
	private final AcrossModule module;

	private final String moduleName;
	private final boolean enabled;

	private AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;
	private ModuleBootstrapStatus bootstrapStatus;
	private ModuleBootstrapConfig bootstrapConfiguration;

	private Collection<AcrossModuleInfo> requiredDependencies =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );
	private Collection<AcrossModuleInfo> optionalDependencies =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );

	private ExposedModuleBeanRegistry exposedBeanRegistry;

	private AcrossModuleSettings settings;

	public ConfigurableAcrossModuleInfo( AcrossContextInfo context, AcrossModule module, int index ) {
		this.context = context;
		this.module = module;
		this.index = index;

		moduleName = module.getName();
		enabled = module.isEnabled();
		bootstrapStatus = enabled ? ModuleBootstrapStatus.AwaitingBootstrap : ModuleBootstrapStatus.Disabled;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public AcrossContextInfo getContextInfo() {
		return context;
	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public String getDescription() {
		return getModule().getDescription();
	}

	@Override
	public String getResourcesKey() {
		return getModule().getResourcesKey();
	}

	@Override
	public AcrossModule getModule() {
		return module;
	}

	@Override
	public Collection<AcrossModuleInfo> getRequiredDependencies() {
		return requiredDependencies;
	}

	public void setRequiredDependencies( Collection<AcrossModuleInfo> requiredDependencies ) {
		this.requiredDependencies = Collections.unmodifiableCollection( requiredDependencies );
	}

	public void setOptionalDependencies( Collection<AcrossModuleInfo> optionalDependencies ) {
		this.optionalDependencies = Collections.unmodifiableCollection( optionalDependencies );
	}

	@Override
	public Collection<AcrossModuleInfo> getOptionalDependencies() {
		return optionalDependencies;
	}

	@Override
	public AcrossModuleRole getModuleRole() {
		return moduleRole;
	}

	public void setModuleRole( AcrossModuleRole moduleRole ) {
		this.moduleRole = moduleRole;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isBootstrapped() {
		return bootstrapStatus == ModuleBootstrapStatus.Bootstrapped;
	}

	public void setBootstrapStatus( ModuleBootstrapStatus bootstrapStatus ) {
		this.bootstrapStatus = bootstrapStatus;
	}

	@Override
	public ModuleBootstrapStatus getBootstrapStatus() {
		return bootstrapStatus;
	}

	@Override
	public ModuleBootstrapConfig getBootstrapConfiguration() {
		return bootstrapConfiguration;
	}

	public ExposedModuleBeanRegistry getExposedBeanRegistry() {
		return exposedBeanRegistry;
	}

	public void setExposedBeanRegistry( ExposedModuleBeanRegistry exposedBeanRegistry ) {
		this.exposedBeanRegistry = exposedBeanRegistry;
	}

	@Override
	public AcrossModuleSettings getSettings() {
		return settings;
	}

	public void setSettings( AcrossModuleSettings settings ) {
		this.settings = settings;
	}

	@Override
	public Map<String, ExposedBeanDefinition> getExposedBeanDefinitions() {
		return exposedBeanRegistry != null
				? exposedBeanRegistry.getExposedDefinitions()
				: Collections.<String, ExposedBeanDefinition>emptyMap();
	}

	public void setBootstrapConfiguration( ModuleBootstrapConfig bootstrapConfiguration ) {
		this.bootstrapConfiguration = bootstrapConfiguration;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return AcrossContextUtils.getApplicationContext( module );
	}
}
