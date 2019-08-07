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

import com.foreach.across.core.context.beans.PrimarySingletonBean;
import com.foreach.across.core.context.beans.SingletonBean;

/**
 * Simple configurer to provide a singleton bean.
 */
public class SingletonBeanConfigurer extends ProvidedBeansConfigurer
{
	public SingletonBeanConfigurer( String beanName, Object value ) {
		this( beanName, value, false );
	}

	public SingletonBeanConfigurer( String beanName, Object value, boolean makePrimary ) {
		addBean( beanName, makePrimary ? new PrimarySingletonBean( value ) : new SingletonBean( value ) );
	}
}
