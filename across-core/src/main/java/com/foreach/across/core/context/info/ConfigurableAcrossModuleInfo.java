/*
 * Copyright 2019 the original author or authors
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
import com.foreach.across.core.AcrossVersionInfo;
import com.foreach.across.core.DynamicAcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.ExposedModuleBeanRegistry;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.module.AcrossModuleBootstrapConfiguration;
import com.foreach.across.core.context.module.AcrossModuleDescriptor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConfigurableAcrossModuleInfo implements AcrossModuleInfo
{
	@Getter
	private final int index;

	@Getter
	private final AcrossContextInfo contextInfo;

	private AcrossModule module;

	@Getter
	private String name;

	@Getter
	private AcrossVersionInfo versionInfo;

	private String[] aliases;

	@Getter
	private String resourcesKey;

	@Getter
	@Setter
	private boolean enabled;

	private AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;

	@Getter
	@Setter
	private int orderInModuleRole;

	private ModuleBootstrapStatus bootstrapStatus;
	private ModuleBootstrapConfig bootstrapConfiguration;

	private Collection<AcrossModuleInfo> requiredDependencies = Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );
	private Collection<AcrossModuleInfo> optionalDependencies = Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );

	private ExposedModuleBeanRegistry exposedBeanRegistry;

	private AcrossModuleDescriptor moduleDescriptor;

	@Getter
	private AcrossModuleBootstrapConfiguration moduleBootstrapConfiguration;

	public ConfigurableAcrossModuleInfo( @NonNull AcrossContextInfo context,
	                                     @NonNull AcrossModuleBootstrapConfiguration moduleBootstrapConfiguration,
	                                     int index ) {
		this.contextInfo = context;
		this.index = index;
		this.moduleBootstrapConfiguration = moduleBootstrapConfiguration;

		moduleDescriptor = moduleBootstrapConfiguration.getModuleDescriptor();
		name = moduleDescriptor.getModuleName();
		resourcesKey = moduleDescriptor.getResourcesKey();
		enabled = moduleDescriptor.isEnabled();
		versionInfo = moduleDescriptor.getVersionInfo();
	}

	public ConfigurableAcrossModuleInfo( AcrossContextInfo context, AcrossModule module, int index ) {
		this.contextInfo = context;
		this.module = module;
		this.index = index;

		name = module.getName();
		enabled = module.isEnabled();
		aliases = module instanceof DynamicAcrossModule ? new String[] { module.getClass().getSimpleName() } : new String[0];
		bootstrapStatus = enabled ? ModuleBootstrapStatus.AwaitingBootstrap : ModuleBootstrapStatus.Disabled;

		resourcesKey = module.getResourcesKey();
		versionInfo = module.getVersionInfo();
	}

	@Override
	public String[] getAliases() {
		return aliases;
	}

	@Override
	public boolean matchesModuleName( @NonNull String moduleName ) {
		return StringUtils.equals( this.name, moduleName ) || ArrayUtils.contains( getAliases(), moduleName );
	}

	@Override
	public String getDescription() {
		return getModule().getDescription();
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

	public ConfigurableListableBeanFactory getBeanFactory() {
		return (ConfigurableListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
	}
}
