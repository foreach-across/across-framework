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

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.AcrossVersionInfo;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.ExposedContextBeanRegistry;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public class ConfigurableAcrossContextInfo implements AcrossContextInfo
{
	private final String id;
	private final AcrossContext context;

	private boolean bootstrapped;
	private Collection<AcrossModuleInfo> configuredModules =
			Collections.unmodifiableCollection( Collections.<AcrossModuleInfo>emptyList() );

	private AcrossBootstrapConfig bootstrapConfiguration;

	private ExposedContextBeanRegistry exposedBeanRegistry;

	public ConfigurableAcrossContextInfo( AcrossContext context ) {
		this.id = context.getId();
		this.context = context;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public AcrossContext getContext() {
		return context;
	}

	@Override
	public String getDisplayName() {
		return context.getDisplayName();
	}

	@Override
	public Collection<AcrossModuleInfo> getConfiguredModules() {
		return configuredModules;
	}

	public void setConfiguredModules( Collection<AcrossModuleInfo> configuredModules ) {
		this.configuredModules = Collections.unmodifiableCollection( configuredModules );
	}

	@Override
	public Collection<AcrossModuleInfo> getModules() {
		Collection<AcrossModuleInfo> modules = new LinkedList<>();

		for ( AcrossModuleInfo module : configuredModules ) {
			if ( module.isEnabled() ) {
				modules.add( module );
			}
		}

		return Collections.unmodifiableCollection( modules );
	}

	@Override
	public boolean hasModule( String moduleName ) {
		return getModuleInfo( moduleName ) != null;
	}

	@Override
	public AcrossModuleInfo getModuleInfo( String moduleName ) {
		for ( AcrossModuleInfo module : configuredModules ) {
			if ( StringUtils.equals( moduleName, module.getName() ) ) {
				return module;
			}
		}

		return null;
	}

	public ConfigurableAcrossModuleInfo getConfigurableModuleInfo( String moduleName ) {
		return (ConfigurableAcrossModuleInfo) getModuleInfo( moduleName );
	}

	@Override
	public AcrossModuleInfo getModuleBeingBootstrapped() {
		for ( AcrossModuleInfo moduleInfo : getModules() ) {
			if ( moduleInfo.getBootstrapStatus() == ModuleBootstrapStatus.BootstrapBusy ) {
				return moduleInfo;
			}
		}

		return null;
	}

	@Override
	public boolean isBootstrapped() {
		return bootstrapped;
	}

	public void setBootstrapped( boolean bootstrapped ) {
		this.bootstrapped = bootstrapped;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return AcrossContextUtils.getApplicationContext( context );
	}

	@Override
	public AcrossBootstrapConfig getBootstrapConfiguration() {
		return bootstrapConfiguration;
	}

	public void setBootstrapConfiguration( AcrossBootstrapConfig bootstrapConfiguration ) {
		this.bootstrapConfiguration = bootstrapConfiguration;
	}

	@Override
	public int getModuleIndex( String moduleName ) {
		for ( AcrossModuleInfo moduleInfo : configuredModules ) {
			if ( StringUtils.equals( moduleName, moduleInfo.getName() ) ) {
				return moduleInfo.getIndex();
			}
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public int getModuleIndex( @NonNull AcrossModule module ) {
		return getModuleIndex( module.getName() );
	}

	@Override
	public int getModuleIndex( @NonNull AcrossModuleInfo moduleInfo ) {
		return getModuleIndex( moduleInfo.getName() );
	}

	public ExposedContextBeanRegistry getExposedBeanRegistry() {
		return exposedBeanRegistry;
	}

	public void setExposedBeanRegistry( ExposedContextBeanRegistry exposedBeanRegistry ) {
		this.exposedBeanRegistry = exposedBeanRegistry;
	}

	@Override
	public Map<String, ExposedBeanDefinition> getExposedBeanDefinitions() {
		return exposedBeanRegistry != null
				? exposedBeanRegistry.getExposedDefinitions()
				: Collections.<String, ExposedBeanDefinition>emptyMap();
	}

	@Override
	public AcrossVersionInfo getVersionInfo() {
		return context.getVersionInfo();
	}
}
