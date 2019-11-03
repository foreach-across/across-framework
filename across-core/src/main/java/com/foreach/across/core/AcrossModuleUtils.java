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
package com.foreach.across.core;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Across context related utility methods for modules.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@UtilityClass
public class AcrossModuleUtils
{
	/**
	 * Returns the created {@link AcrossContextBeanRegistry} for the context this module belongs to.
	 * The module itself does not need to have bootstrapped but the context it is attached to should.
	 * {@code null} will be returned if no bean registry is available.
	 *
	 * @return AcrossContextBeanRegistry of the running context (null if none).
	 */
	public static AcrossContextBeanRegistry acrossContextBeanRegistry( @NonNull AcrossModuleInfo moduleInfo ) {
		AcrossContextInfo contextInfo = moduleInfo.getContextInfo();

		if ( contextInfo.isBootstrapped() ) {
			return contextInfo.getApplicationContext().getBean( AcrossContextBeanRegistry.class );
		}

		return null;
	}

	/**
	 * Returns the {@link AcrossListableBeanFactory} attached to a module. Will throw an {@link IllegalStateException}
	 * if the module has not bootstrapped and no bean factory is available.
	 *
	 * @param moduleInfo Across module
	 * @return beanfactory
	 */
	@org.springframework.lang.NonNull
	public static AcrossListableBeanFactory beanFactory( @NonNull AcrossModuleInfo moduleInfo ) {
		return (AcrossListableBeanFactory) moduleInfo.getApplicationContext().getAutowireCapableBeanFactory();
	}
}
