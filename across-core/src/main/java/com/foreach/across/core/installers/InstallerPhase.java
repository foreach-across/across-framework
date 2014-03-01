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
	AfterContextBoostrap
}
