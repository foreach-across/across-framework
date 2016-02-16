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

import com.foreach.across.core.annotations.AcrossRole;
import com.foreach.across.core.context.AcrossModuleRole;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * Dynamic instance of an Across module.
 *
 * @author Arne Vandamme
 */
public abstract class DynamicAcrossModule extends AcrossModule implements Ordered
{
	private int order;
	private String name;
	private String resourcesKey;

	private String[] installerScanPackages = new String[0];
	private String[] moduleConfigurationScanPackages = new String[0];

	/**
	 * Only the {@link DynamicAcrossModuleFactory} can create instances.
	 */
	protected DynamicAcrossModule() {
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
	}

	@Override
	protected void registerDefaultInstallerContextConfigurers( Set<ApplicationContextConfigurer> installerContextConfigurers ) {
	}

	@Override
	public int getOrder() {
		return order;
	}

	void setOrder( int order ) {
		this.order = order;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName( String name ) {
		this.name = name;
	}

	@Override
	public String getResourcesKey() {
		return resourcesKey;
	}

	void setResourcesKey( String resourcesKey ) {
		this.resourcesKey = resourcesKey;
	}

	@Override
	public String getDescription() {
		return "Dynamic module - generated by a DynamicAcrossModuleFactory";
	}

	@Override
	public String[] getInstallerScanPackages() {
		return installerScanPackages;
	}

	void setInstallerScanPackages( String... installerScanPackages ) {
		this.installerScanPackages = installerScanPackages;
	}

	@Override
	public String[] getModuleConfigurationScanPackages() {
		return moduleConfigurationScanPackages;
	}

	void setModuleConfigurationScanPackages( String... moduleConfigurationScanPackages ) {
		this.moduleConfigurationScanPackages = moduleConfigurationScanPackages;
	}

	@AcrossRole(value = AcrossModuleRole.APPLICATION)
	public static final class DynamicApplicationModule extends DynamicAcrossModule
	{
		DynamicApplicationModule() {
		}
	}

	@AcrossRole(value = AcrossModuleRole.INFRASTRUCTURE)
	public static final class DynamicInfrastructureModule extends DynamicAcrossModule
	{
		DynamicInfrastructureModule() {
		}
	}

	@AcrossRole(value = AcrossModuleRole.POSTPROCESSOR)
	public static final class DynamicPostProcessorModule extends DynamicAcrossModule
	{
		DynamicPostProcessorModule() {
		}
	}
}
