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

package com.foreach.across.core.context.configurer;

import com.foreach.across.core.context.beans.ProvidedBeansMap;

import java.util.Map;

/**
 * Allows specifying a ProvidedBeansMap to provide to an ApplicationContext.
 */
public class ProvidedBeansConfigurer extends ApplicationContextConfigurerAdapter
{
	private ProvidedBeansMap providedBeans;

	public ProvidedBeansConfigurer() {
		this( new ProvidedBeansMap() );
	}

	public ProvidedBeansConfigurer( Map<String, Object> beansMap ) {
		this( new ProvidedBeansMap( beansMap ) );
	}

	public ProvidedBeansConfigurer( ProvidedBeansMap providedBeans ) {
		this.providedBeans = providedBeans;
	}

	/**
	 * Returns a map of beans to register directly in the ApplicationContext.
	 * Provided beans will be registered first, before any of the annotated classes
	 * or defined packages are loaded.
	 *
	 * @return Map of bean name and value.
	 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
	 */
	@Override
	public ProvidedBeansMap providedBeans() {
		return providedBeans;
	}

	public void addBean( String name, Object value ) {
		providedBeans.put( name, value );
	}
}
