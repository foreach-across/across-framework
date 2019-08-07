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

import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * Helper class that wraps around a singleton to register it as primary bean under that name.
 */
public class PrimarySingletonBean extends SingletonBean
{
	public PrimarySingletonBean( Object object ) {
		this( object, new AutowireCandidateQualifier[0] );
	}

	public PrimarySingletonBean( Object object, AutowireCandidateQualifier... qualifiers ) {
		super( object );

		GenericBeanDefinition definition = new GenericBeanDefinition();
		definition.setPrimary( true );

		for ( AutowireCandidateQualifier qualifier : qualifiers ) {
			definition.addQualifier( qualifier );
		}

		setBeanDefinition( definition );
	}
}
