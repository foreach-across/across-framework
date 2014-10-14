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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBeanRenameTransformer implements ExposedBeanDefinitionTransformer
{
	@SuppressWarnings("all")
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	/**
	 * Modify the collection of ExposedBeanDefinitions.
	 *
	 * @param beanDefinitions Map of exposed bean definitions.
	 */
	public void transformBeanDefinitions( Map<String, ExposedBeanDefinition> beanDefinitions ) {
		List<String> removals = new LinkedList<>();

		for ( Map.Entry<String, ExposedBeanDefinition> definition : beanDefinitions.entrySet() ) {
			ExposedBeanDefinition exposed = definition.getValue();
			String name = rename( exposed.getPreferredBeanName(), exposed );

			if ( name == null ) {
				LOG.debug( "Removing exposed bean {} because preferredBeanName was null", definition.getKey() );
				removals.add( definition.getKey() );
			}
			else {
				exposed.setPreferredBeanName( name );
			}
		}

		for ( String removal : removals ) {
			beanDefinitions.remove( removal );
		}
	}

	protected abstract String rename( String beanName, ExposedBeanDefinition definition );
}
