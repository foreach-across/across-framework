package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.installers.InstallerSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents the global bootstrap configuration for the AcrossContext.
 */
public class AcrossBootstrapConfig
{
	private final AcrossContext context;
	private final Collection<ModuleBootstrapConfig> modules;

	private InstallerSettings installerSettings;

	public AcrossBootstrapConfig( AcrossContext context, Collection<ModuleBootstrapConfig> modules ) {
		this.context = context;
		setInstallerSettings( context.getInstallerSettings() );

		// Modifying the module collection itself is no longer allowed in bootstrap phase
		this.modules = Collections.unmodifiableCollection( modules );
	}

	/**
	 * @return The AcrossContext this configuration is attached to.
	 */
	public AcrossContext getContext() {
		return context;
	}

	/**
	 * @return Collection of the module configurations that will be bootstrapped.
	 */
	public Collection<ModuleBootstrapConfig> getModules() {
		return modules;
	}

	public InstallerSettings getInstallerSettings() {
		return installerSettings;
	}

	public void setInstallerSettings( InstallerSettings installerSettings ) {
		Assert.notNull( installerSettings, "InstallerSettings for the AcrossContext can never be null." );
		this.installerSettings = installerSettings;
	}

	/**
	 * @param moduleName Unique name of the module.
	 * @return True if the module with that name is configured on the context.
	 */
	public boolean hasModule( String moduleName ) {
		return getModule( moduleName ) != null;
	}

	/**
	 * @param moduleName Unique name of the module.
	 * @return Bootstrap configuration instance for that module.
	 */
	public ModuleBootstrapConfig getModule( String moduleName ) {
		for ( ModuleBootstrapConfig config : modules ) {
			if ( StringUtils.equals( moduleName, config.getModuleName() ) ) {
				return config;
			}
		}

		return null;
	}

	/**
	 * Method to add one or more configuration classes to a module bootstrap configuration.
	 * The module is identified by its name.  This method is safe to use in all circumstances:
	 * if the module is not configured in the context only the return value will be false but no
	 * exception will occur.
	 *
	 * @param moduleName           Unique name of the module in the context.
	 * @param configurationClasses Annotated class instances.
	 * @return True if the module was present.
	 */
	public boolean extendModule( String moduleName, Class... configurationClasses ) {
		return extendModule( moduleName, new AnnotatedClassConfigurer( configurationClasses ) );
	}

	/**
	 * Method to add one or more configurers to a module bootstrap configuration.
	 * The module is identified by its name.  This method is safe to use in all circumstances:
	 * if the module is not configured in the context only the return value will be false but no
	 * exception will occur.
	 *
	 * @param moduleName  Unique name of the module in the context.
	 * @param configurers Configurers to add to the bootstrap.
	 * @return True if the module was present.
	 */
	public boolean extendModule( String moduleName, ApplicationContextConfigurer... configurers ) {
		if ( hasModule( moduleName ) ) {
			getModule( moduleName ).addApplicationContextConfigurer( configurers );
			return true;
		}

		return false;
	}
}
