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
package com.foreach.across.core.context;

import com.foreach.across.core.AcrossModule;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Implementation of {@link ModuleDependencyResolver} that uses a {@link ClassPathScanningCandidateModuleProvider}
 * to determine the modules that can be resolved.
 *
 * @author Arne Vandamme
 */
public class ClassPathScanningModuleDependencyResolver implements ModuleDependencyResolver
{
	private ClassPathScanningCandidateModuleProvider candidateModuleProvider
			= new ClassPathScanningCandidateModuleProvider();

	private boolean resolveRequired = true;
	private boolean resolveOptional = false;
	private Map<String, Supplier<AcrossModule>> candidates = Collections.emptyMap();

	/**
	 * @param basePackages that should be scanned
	 */
	public void setBasePackages( String... basePackages ) {
		candidates = candidateModuleProvider.findCandidateModules( basePackages );
	}

	/**
	 * @param resolveRequired should required modules be resolved
	 */
	public void setResolveRequired( boolean resolveRequired ) {
		this.resolveRequired = resolveRequired;
	}

	/**
	 * @param resolveOptional should optional modules be resolved
	 */
	public void setResolveOptional( boolean resolveOptional ) {
		this.resolveOptional = resolveOptional;
	}

	public boolean isResolveRequired() {
		return resolveRequired;
	}

	public boolean isResolveOptional() {
		return resolveOptional;
	}

	@Override
	public Optional<AcrossModule> resolveModule( String moduleName, boolean requiredModule ) {
		if ( requiredModule && isResolveRequired() || !requiredModule && isResolveOptional() ) {
			if ( candidates.containsKey( moduleName ) ) {
				return Optional.ofNullable( candidates.get( moduleName ).get() );
			}
		}

		return Optional.empty();
	}
}
