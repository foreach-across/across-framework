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
package com.foreach.across.core;

import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.ClassPathScanningChildPackageProvider;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * Factory bean that creates a {@link DynamicAcrossModule} instance based on a root package and the parameters
 * passed in.  If a package is all that is provided, the name of the module will be derived from the lowest package
 * name and all default sub packages will be scanned.
 *
 * @author Arne Vandamme
 */
public class DynamicAcrossModuleFactory implements FactoryBean<DynamicAcrossModule>
{
	private final ClassPathScanningChildPackageProvider packageProvider = new ClassPathScanningChildPackageProvider();

	private AcrossModuleRole moduleRole = AcrossModuleRole.APPLICATION;
	private String moduleName, resourcesKey;
	private String basePackage, shortPackageName;
	private Integer order;
	private boolean fullComponentScan = true;

	/**
	 * Set the module role that the generated module should have, defaults to {@link AcrossModuleRole#APPLICATION}.
	 *
	 * @param moduleRole instance
	 * @return self
	 */
	public DynamicAcrossModuleFactory setModuleRole( AcrossModuleRole moduleRole ) {
		this.moduleRole = moduleRole;
		return this;
	}

	/**
	 * Set the order of the module within the role scope.
	 *
	 * @param order index
	 * @return self
	 */
	public DynamicAcrossModuleFactory setOrder( int order ) {
		this.order = order;
		return this;
	}

	/**
	 * Set the name for the generated module.
	 *
	 * @param moduleName unique name
	 * @return self
	 */
	public DynamicAcrossModuleFactory setModuleName( String moduleName ) {
		this.moduleName = moduleName;
		return this;
	}

	/**
	 * Set the resources key for the generated module.  Will be same as module name if name is set, will be
	 * same as the lowest package name if only a base package is set.
	 *
	 * @param resourcesKey path for resources
	 * @return self
	 */
	public DynamicAcrossModuleFactory setResourcesKey( String resourcesKey ) {
		this.resourcesKey = resourcesKey;
		return this;
	}

	/**
	 * Typed variant of {@link #setBasePackage(String)}.
	 *
	 * @param basePackageClass class contained in the base package
	 * @return self
	 */
	public DynamicAcrossModuleFactory setBasePackageClass( Class<?> basePackageClass ) {
		setBasePackage( basePackageClass.getPackage().getName() );
		return this;
	}

	/**
	 * Should full component scan be enabled for the module (default), or should only the config
	 * child package be scanned?
	 *
	 * @param fullComponentScan true if full component scan should be enabled
	 * @return self
	 */
	public DynamicAcrossModuleFactory setFullComponentScan( boolean fullComponentScan ) {
		this.fullComponentScan = fullComponentScan;
		return this;
	}

	/**
	 * Base package for the module.  The lowest package name from the base package will be determined for the
	 * module name if no name is given explicitly.  If auto-scan is enabled, all child packages will be scanned
	 * for components.
	 *
	 * @param basePackage package name
	 * @return self
	 */
	public DynamicAcrossModuleFactory setBasePackage( String basePackage ) {
		Assert.notNull( basePackage );

		this.basePackage = basePackage;

		Package pkg = Package.getPackage( basePackage );
		shortPackageName = pkg.getName().contains( "." )
				? StringUtils.substringAfterLast( pkg.getName(), "." ) : pkg.getName();

		return this;
	}

	@Override
	public DynamicAcrossModule getObject() throws Exception {
		if ( moduleName == null && basePackage == null ) {
			throw new IllegalStateException( "Either module name or base package must be specified." );
		}

		DynamicAcrossModule module = createWithOrder();
		module.setName( getModuleName() );
		module.setResourcesKey( getResourcesKey() );

		module.addApplicationContextConfigurer( new ComponentScanConfigurer( buildComponentScanPackages() ) );
		module.addInstallerContextConfigurer( new ComponentScanConfigurer( basePackage + ".installers.config" ) );

		module.setInstallerScanPackages( basePackage + ".installers" );
		module.setModuleConfigurationScanPackages( basePackage + ".config", basePackage + ".extensions" );
		return module;
	}

	private String[] buildComponentScanPackages() {
		if ( basePackage != null ) {
			if ( fullComponentScan ) {
				synchronized ( packageProvider ) {
					packageProvider.setExcludedChildPackages( "installers", "extensions" );
					return packageProvider.findChildren( basePackage );
				}
			}
			else {
				return new String[] { basePackage + ".config" };
			}
		}

		return new String[0];
	}

	private DynamicAcrossModule createWithOrder() {
		DynamicAcrossModule module;
		int defaultOrder = Ordered.LOWEST_PRECEDENCE;

		switch ( moduleRole ) {
			case INFRASTRUCTURE:
				module = new DynamicAcrossModule.DynamicInfrastructureModule();
				defaultOrder = Ordered.HIGHEST_PRECEDENCE;
				break;
			case POSTPROCESSOR:
				module = new DynamicAcrossModule.DynamicPostProcessorModule();
				break;
			default:
				module = new DynamicAcrossModule.DynamicApplicationModule();
				break;
		}

		module.setOrder( order != null ? order : defaultOrder );

		return module;
	}

	private String getModuleName() {
		if ( moduleName != null ) {
			return moduleName;
		}

		return StringUtils.capitalize( shortPackageName ) + "Module";
	}

	private String getResourcesKey() {
		if ( resourcesKey != null ) {
			return resourcesKey;
		}

		if ( moduleName != null ) {
			return moduleName;
		}

		return StringUtils.uncapitalize( shortPackageName );
	}

	@Override
	public Class<?> getObjectType() {
		return DynamicAcrossModule.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
