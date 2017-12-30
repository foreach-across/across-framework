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

package com.foreach.across.core.events;

import com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;

/**
 * Event that is sent right before the module is bootstrapped.
 * The ModuleBootstrapConfig can still be modified at this point.
 * <p/>
 * Source of this event is the {@link AcrossContextInfo} where the module is being created.
 *
 * @see com.foreach.across.core.context.bootstrap.ModuleBootstrapConfig
 */
public final class AcrossModuleBeforeBootstrapEvent extends AcrossLifecycleEvent
{
	private final AcrossContextInfo context;
	private final AcrossModuleInfo module;

	public AcrossModuleBeforeBootstrapEvent( AcrossContextInfo context, AcrossModuleInfo module ) {
		super( context );
		this.context = context;
		this.module = module;
	}

	public AcrossContextInfo getContext() {
		return context;
	}

	public AcrossModuleInfo getModule() {
		return module;
	}

	public ModuleBootstrapConfig getBootstrapConfig() {
		return module.getBootstrapConfiguration();
	}

	/**
	 * @return the Across context where the module is being bootstrapped
	 */
	@Override
	public AcrossContextInfo getSource() {
		return (AcrossContextInfo) super.getSource();
	}

	/**
	 * Add any number of configurers to the module being bootstrapped.
	 *
	 * @param configurers One or more ApplicationContextConfigurer instances to add.
	 * @deprecated use {@link #getBootstrapConfig()} to alter the bootstrap configuration
	 */
	@Deprecated
	public void addApplicationContextConfigurers( ApplicationContextConfigurer... configurers ) {
		getBootstrapConfig().addApplicationContextConfigurer( configurers );
	}

	@Override
	public String toString() {
		return "AcrossModuleBeforeBootstrapEvent{" +
				"context=" + context.getDisplayName() +
				", module=" + module.getName() +
				'}';
	}
}
