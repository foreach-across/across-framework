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
package com.foreach.across.core.context.support;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossModuleRole;

import java.util.*;

/**
 * Represents a defined list of {@link com.foreach.across.core.AcrossModule} instances and
 * their basic meta information.
 *
 * @author Arne Vandamme
 */
public class ModuleSet
{
	protected Map<String, AcrossModule> modules = new LinkedHashMap<>();
	protected Map<AcrossModule, AcrossModuleRole> moduleRoles = new HashMap<>();
	protected Map<AcrossModule, Collection<String>> definedRequiredDependencies = new HashMap<>();
	protected Map<AcrossModule, Collection<String>> definedOptionalDependencies = new HashMap<>();

	/**
	 * @return list of configured modules
	 */
	public List<AcrossModule> getModules() {
		return new ArrayList<>( modules.values() );
	}

	/**
	 * @return map of configured modules with the module name being the key
	 */
	public Map<String, AcrossModule> getModuleMap() {
		return Collections.unmodifiableMap( modules );
	}

	public Collection<String> getRequiredDependencies( AcrossModule module ) {
		return definedRequiredDependencies.get( module );
	}

	public Collection<String> getOptionalDependencies( AcrossModule module ) {
		return definedOptionalDependencies.get( module );
	}

	public AcrossModuleRole getModuleRole( AcrossModule module ) {
		return moduleRoles.get( module );
	}
}
