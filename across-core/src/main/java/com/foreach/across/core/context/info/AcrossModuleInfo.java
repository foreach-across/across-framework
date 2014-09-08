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
import com.foreach.across.core.context.AcrossEntity;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ExposedBeanDefinition;
import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;

public interface AcrossModuleInfo extends AcrossEntity
{
	/**
	 * @return The info object of the AcrossContext this module belongs to.
	 */
	AcrossContextInfo getContextInfo();

	/**
	 * @return The index of the module in the context bootstrap.
	 */
	int getIndex();

	/**
	 * @return Name of the AcrossModule.
	 */
	String getName();

	/**
	 * @return Description of the AcrossModule.
	 */
	String getDescription();

	/**
	 * @return AcrossModule instance.
	 */
	AcrossModule getModule();

	/**
	 * @return Collection of required module dependencies.
	 */
	Collection<AcrossModuleInfo> getRequiredDependencies();

	/**
	 * @return Collection of optional module dependencies.
	 */
	Collection<AcrossModuleInfo> getOptionalDependencies();

	/**
	 * @return True if the module was enabled and should be bootstrapped.
	 */
	boolean isEnabled();

	/**
	 * @return True if the module has been bootstrapped.
	 */
	boolean isBootstrapped();

	/**
	 * @return Actual status of the module boostrap.
	 */
	ModuleBootstrapStatus getBootstrapStatus();

	/**
	 * @return Configuration object used for bootstrapping this module.  Null if module was disabled.
	 */
	ModuleBootstrapConfig getBootstrapConfiguration();

	/**
	 * @return The Spring application context for this module.
	 */
	ApplicationContext getApplicationContext();

	/**
	 * @return The specific role of the module in the context.
	 */
	AcrossModuleRole getModuleRole();

	/**
	 * @return The collection of exposed BeanDefinitions.
	 */
	Map<String, ExposedBeanDefinition> getExposedBeanDefinitions();

	/**
	 * @return The settings used to configure this module.
	 */
	AcrossModuleSettings getSettings();
}
