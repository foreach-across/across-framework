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

package com.foreach.across.core.installers;

/**
 * Determines during which phase the installer should execute.  The phase determines which beans will be available.
 * Before a bootstrap phase, the beans in the module packages will not have been created, this is done during the bootstrap.
 * <ul>
 * <li>BeforeContextBootstrap: before any module has bootstrapped (usually for schema installers)</li>
 * <li>BeforeModuleBootstrap: after previous modules have bootstrapped, but before the current module does</li>
 * <li>AfterModuleBootstrap: after the current and previous modules have bootstrapped</li>
 * <li>AfterContextBootstrap: after all modules in the context have bootstrapped</li>
 * </ul>
 */
public enum InstallerPhase
{
	BeforeContextBootstrap,
	BeforeModuleBootstrap,
	AfterModuleBootstrap,
	AfterContextBootstrap
}
