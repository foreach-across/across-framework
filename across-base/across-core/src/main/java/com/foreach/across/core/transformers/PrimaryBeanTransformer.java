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

package com.foreach.across.core.transformers;

import com.foreach.across.core.context.ExposedBeanDefinition;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;

/**
 * <p>Will register the bean definitions as primary beans.  Optionally a list of the bean names
 * that should be set primary can be defined.</p>
 * <p>Note that this will in fact modify the original BeanDefinition.</p>
 */
public class PrimaryBeanTransformer implements ExposedBeanDefinitionTransformer
{
	private Collection<String> beanNames;

	public PrimaryBeanTransformer() {
	}

	public PrimaryBeanTransformer( Collection<String> beanNames ) {
		this.beanNames = beanNames;
	}

	public void setBeanNames( @NonNull Collection<String> beanNames ) {
		this.beanNames = beanNames;
	}

	public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
		for ( Map.Entry<String, ExposedBeanDefinition> definition : beanDefinitions.entrySet() ) {
			makePrimary( definition.getKey(), definition.getValue() );
		}
	}

	private void makePrimary( String beanName, ExposedBeanDefinition definition ) {
		if ( !definition.isPrimary() && ( beanNames == null || beanNames.contains( beanName ) ) ) {
			definition.setPrimary( true );
		}
	}
}
