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

package com.foreach.across.core.events;

import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * Event published after the AcrossContext has bootstrapped.
 * That means full initialization has happened: all modules are started and all installers
 * have been executed, including those registered {@link com.foreach.across.core.installers.InstallerPhase#AfterContextBootstrap}.
 * <p/>
 * Source of this event will be the root {@link org.springframework.context.ApplicationContext} of the Across context or its parent
 * {@link ApplicationContext} if it has one (which is usually the case).
 */
public final class AcrossContextBootstrappedEvent extends AcrossLifecycleEvent
{
	private final AcrossContextInfo contextInfo;

	public AcrossContextBootstrappedEvent( AcrossContextInfo contextInfo ) {
		super(
				contextInfo.getApplicationContext().getParent() != null
						? contextInfo.getApplicationContext().getParent() : contextInfo.getApplicationContext()
		);
		this.contextInfo = contextInfo;
	}

	/**
	 * @return info of the bootstrapped Across context
	 */
	public AcrossContextInfo getContext() {
		return contextInfo;
	}

	/**
	 * The Spring {@link ApplicationContext} that initiated the event. Either the root {@link ApplicationContext} of the
	 * Across context, or the {@link ApplicationContext} owning the {@link com.foreach.across.core.AcrossContext} bean.
	 *
	 * @return Spring application context
	 */
	@Override
	public ApplicationContext getSource() {
		return (ApplicationContext) super.getSource();
	}

	/**
	 * @return The actual collection of all modules that were supposed to have bootstrapped.
	 * @deprecated since 5.0.0 - use the appropriate method from {@link AcrossContextInfo} instead, keeping into account the bootstrap status
	 */
	@Deprecated
	public Collection<AcrossModuleInfo> getModules() {
		return contextInfo.getBootstrappedModules();
	}

	@Override
	public String toString() {
		return "AcrossContextBootstrappedEvent{" +
				"context=" + contextInfo.getDisplayName() +
				", modulesBootstrapped=" + contextInfo.getBootstrappedModules().size() +
				'}';
	}
}
