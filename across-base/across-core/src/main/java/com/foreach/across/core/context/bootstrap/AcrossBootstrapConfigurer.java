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
package com.foreach.across.core.context.bootstrap;

/**
 * Interface to be implemented by any bean from a parent context that allows customization
 * of either the {@link AcrossBootstrapConfig} of the context, or any {@link ModuleBootstrapConfig}.
 * <p/>
 * Is a generic alternative to {@link com.foreach.across.core.AcrossModule#prepareForBootstrap(ModuleBootstrapConfig, AcrossBootstrapConfig)}
 * that can be implemented by any bean (in the parent context) or in the Across context.
 * <p/>
 * The module configuration can be customized this way, however, additional modules are not supposed
 * to be added using this inteface.  See {@link com.foreach.across.config.AcrossContextConfigurer} for that.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.config.AcrossContextConfigurer
 * @since 3.0.0
 */
public interface AcrossBootstrapConfigurer
{
	String CONTEXT_INFRASTRUCTURE_MODULE = "AcrossContextInfrastructureModule";
	String CONTEXT_POSTPROCESSOR_MODULE = "AcrossContextPostProcessorModule";

	/**
	 * Change the bootstrap configuration of the {@link com.foreach.across.core.AcrossContext} itself.
	 *
	 * @param contextConfiguration bootstrap configuration of the context
	 */
	default void configureContext( AcrossBootstrapConfig contextConfiguration ) {
	}

	/**
	 * Change a specific module configuration.  Will be called in order, once for every module present.
	 *
	 * @param moduleConfiguration configuration for the module
	 */
	default void configureModule( ModuleBootstrapConfig moduleConfiguration ) {
	}
}
