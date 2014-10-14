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
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * Wrapper to force an instance to be configured as a singleton in a ProvidedBeansMap.
 * Useful if you want to register a BeanDefinition as a singleton instead,
 * or if you want to register both singleton and a corresponding BeanDefinition.
 *
 * @see com.foreach.across.core.context.beans.PrimarySingletonBean
 * @see com.foreach.across.core.context.beans.ProvidedBeansMap
 */
public class SingletonBean
{
	private Object object;
	private BeanDefinition beanDefinition;

	public SingletonBean( Object object ) {
		this.object = object;
	}

	public SingletonBean( Object object, AutowireCandidateQualifier... qualifiers ) {
		this( object );

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setPrimary( false );

		for ( AutowireCandidateQualifier qualifier : qualifiers ) {
			definition.addQualifier( qualifier );
		}

		setBeanDefinition( definition );
	}

	public SingletonBean( Object object, BeanDefinition beanDefinition ) {
		this.object = object;
		this.beanDefinition = beanDefinition;
	}

	public Object getObject() {
		return object;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	protected void setObject( Object object ) {
		this.object = object;
	}

	protected void setBeanDefinition( BeanDefinition beanDefinition ) {
		this.beanDefinition = beanDefinition;
	}
}
