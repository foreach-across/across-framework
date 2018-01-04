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

import java.util.Optional;

/**
 * Responsible for resolving an {@link com.foreach.across.core.AcrossModule} by name.
 *
 * @author Arne Vandamme
 */
public interface ModuleDependencyResolver
{
	/**
	 * Attempts to resolve the module by name.  The success of a resolving a module might depend on the required
	 * parameter, as a resolver could be configured to return required but not optional modules.
	 *
	 * @param moduleName     Name of the module.
	 * @param requiredModule If this is a required dependency.
	 * @return module instance
	 */
	Optional<AcrossModule> resolveModule( String moduleName, boolean requiredModule );
}
