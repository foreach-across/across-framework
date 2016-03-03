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

import com.foreach.across.core.context.info.ConfigurableAcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.filters.BeanFilter;
import com.foreach.across.core.transformers.ExposedBeanDefinitionTransformer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Builds a set of BeanDefinitions that should be exposed to other contexts.
 *
 * @author Arne Vandamme
 */
public class ExposedModuleBeanRegistry extends AbstractExposedBeanRegistry
{
	private final BeanDefinitionRegistry beanDefinitionRegistry;

	public ExposedModuleBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry,
	                                  ConfigurableAcrossModuleInfo moduleInfo,
	                                  ConfigurableApplicationContext child,
	                                  BeanFilter filter,
	                                  ExposedBeanDefinitionTransformer transformer ) {
		super( contextBeanRegistry, moduleInfo.getName(), transformer );

		if ( child instanceof BeanDefinitionRegistry ) {
			beanDefinitionRegistry = (BeanDefinitionRegistry) child;
		}
		else {
			beanDefinitionRegistry = null;
		}

		if ( filter == null ) {
			return;
		}

		Map<String, Object> beans = ApplicationContextScanner.findSingletonsMatching( child, filter );
		Map<String, BeanDefinition> definitions =
				ApplicationContextScanner.findBeanDefinitionsMatching( child, filter );

		addBeans( definitions, beans );
	}

	@Override
	protected String[] getAliases( String beanName ) {
		return beanDefinitionRegistry != null ? beanDefinitionRegistry.getAliases( beanName ) : new String[0];
	}
}
