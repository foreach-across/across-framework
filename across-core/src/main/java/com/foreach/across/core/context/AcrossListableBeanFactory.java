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

package com.foreach.across.core.context;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.NewAcrossListableBeanFactory;

/**
 * Extends a {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * with support for Exposed beans.  This implementation also allows the parent bean factory to be updated.
 * <p>
 * Exposed beans are fetched from the module context but are not managed by the bean factory.
 * </p>
 */
@SuppressWarnings("serial")
public class AcrossListableBeanFactory extends NewAcrossListableBeanFactory
{
	public AcrossListableBeanFactory() {
	}

	public AcrossListableBeanFactory( BeanFactory parentBeanFactory ) {
		super( parentBeanFactory );
	}
}

