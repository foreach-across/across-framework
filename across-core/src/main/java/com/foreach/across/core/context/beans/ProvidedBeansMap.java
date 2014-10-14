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

package com.foreach.across.core.context.beans;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Special object map that represent a collection of beans: either singleton
 * or bean definitions (or a combination).  Used to configure a collection of beans
 * that should be registered in an ApplicationContext.</p>
 * <p/>
 * <p>Instance implementing BeanDefinition will be considered as bean definition, all
 * others will be considered as singletons.  Any instance extending SingletonBean can
 * register both a singleton and a BeanDefinition, and is the only way to try to register
 * a BeanDefinition instance as a singleton.</p>
 *
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see com.foreach.across.core.context.beans.SingletonBean
 * @see com.foreach.across.core.context.beans.PrimarySingletonBean
 */
public class ProvidedBeansMap extends HashMap<String, Object>
{
	public ProvidedBeansMap() {
	}

	public ProvidedBeansMap( int initialCapacity ) {
		super( initialCapacity );
	}

	public ProvidedBeansMap( int initialCapacity, float loadFactor ) {
		super( initialCapacity, loadFactor );
	}

	public ProvidedBeansMap( Map<? extends String, ?> m ) {
		super( m );
	}

	/**
	 * @return Map of Singleton objects registered.
	 */
	public Map<String, Object> getSingletons() {
		Map<String, Object> singletons = new HashMap<String, Object>();

		for ( Map.Entry<String, Object> bean : entrySet() ) {
			Object value = bean.getValue();

			if ( value instanceof SingletonBean ) {
				singletons.put( bean.getKey(), ( (SingletonBean) value ).getObject() );
			}
			else if ( !( value instanceof BeanDefinition ) ) {
				singletons.put( bean.getKey(), value );
			}

		}
		return singletons;
	}

	/**
	 * @return Map of BeanDefinitions registered.
	 */
	public Map<String, BeanDefinition> getBeanDefinitions() {
		Map<String, BeanDefinition> definitions = new HashMap<String, BeanDefinition>();

		for ( Map.Entry<String, Object> bean : entrySet() ) {
			Object value = bean.getValue();

			if ( value instanceof SingletonBean ) {
				BeanDefinition definition = ( (SingletonBean) value ).getBeanDefinition();
				if ( definition != null ) {
					definitions.put( bean.getKey(), definition );
				}
			}
			else if ( value instanceof BeanDefinition ) {
				definitions.put( bean.getKey(), (BeanDefinition) value );
			}

		}

		return definitions;
	}
}
