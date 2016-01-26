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

package com.foreach.across.core.context.bootstrap;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossApplicationContextHolder;
import com.foreach.across.core.context.AcrossConfigurableApplicationContext;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * In charge of creating the ApplicationContext and loading all the beans.
 */
public interface BootstrapApplicationContextFactory
{
	/**
	 * Create a new ApplicationContext instance that support Across context behavior.
	 *
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	AcrossConfigurableApplicationContext createApplicationContext();

	/**
	 * Create a new {@link ApplicationContext} for running installers, it should support immediate registration
	 * of bean definitions and singletons (eg {@link com.foreach.across.core.context.AcrossApplicationContext}).
	 *
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	AcrossConfigurableApplicationContext createInstallerContext();

	/**
	 * Create the Spring ApplicationContext for the root of the AcrossContext.
	 * Optionally a parent ApplicationContext can be specified and a map of singletons that are guaranteed
	 * to be available when the ApplicationContext has been created.
	 *
	 * @param across                   AcrossContext being created.
	 * @param parentApplicationContext Parent ApplicationContext, can be null.
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	AcrossConfigurableApplicationContext createApplicationContext( AcrossContext across,
	                                                               ApplicationContext parentApplicationContext );

	/**
	 * Create the Spring ApplicationContext for a particular AcrossModule.
	 *
	 * @param across                AcrossContext being loaded.
	 * @param moduleBootstrapConfig Bootstrap configuration of the AcrossModule being created.
	 * @param parentContext         Contains the parent context.
	 * @return Spring ApplicationContext instance implementing AcrossConfigurableApplicationContext.
	 */
	AcrossConfigurableApplicationContext createApplicationContext( AcrossContext across,
	                                                               ModuleBootstrapConfig moduleBootstrapConfig,
	                                                               AcrossApplicationContextHolder parentContext );

	/**
	 * Loads beans and definitions in the root ApplicationContext.
	 *
	 * @param across  AcrossContext being loaded.
	 * @param context Contains the root Spring ApplicationContext.
	 */
	void loadApplicationContext( AcrossContext across, AcrossApplicationContextHolder context );

	/**
	 * Loads beans and definitions in the module ApplicationContext.
	 *
	 * @param across                AcrossContext being loaded.
	 * @param moduleBootstrapConfig Bootstrap configuration of the AcrossModule being loaded.
	 * @param context               Contains the Spring ApplicationContext for the module.
	 */
	void loadApplicationContext( AcrossContext across,
	                             ModuleBootstrapConfig moduleBootstrapConfig,
	                             AcrossApplicationContextHolder context );

	/**
	 * Loads a set of configurers into an {@link ApplicationContext}.
	 *
	 * @param context     Configurable application context
	 * @param configurers Configurers to apply
	 */
	void loadApplicationContext( AcrossConfigurableApplicationContext context,
	                             Collection<ApplicationContextConfigurer> configurers );
}
