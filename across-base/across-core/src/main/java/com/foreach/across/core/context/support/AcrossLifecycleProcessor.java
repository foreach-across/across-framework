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
package com.foreach.across.core.context.support;

import com.foreach.across.core.context.AcrossListableBeanFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.DefaultLifecycleProcessor;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom implementation of {@link DefaultLifecycleProcessor} which excludes exposed beans.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class AcrossLifecycleProcessor extends DefaultLifecycleProcessor
{
	private AcrossListableBeanFactory lbf;

	@Override
	public void setBeanFactory( BeanFactory beanFactory ) {
		super.setBeanFactory( beanFactory );

		if ( beanFactory instanceof AcrossListableBeanFactory ) {
			lbf = (AcrossListableBeanFactory) beanFactory;
		}
	}

	@Override
	protected Map<String, Lifecycle> getLifecycleBeans() {
		Map<String, Lifecycle> lifecycleBeans = super.getLifecycleBeans();

		if ( lbf != null ) {
			lifecycleBeans.keySet()
			              .stream()
			              .filter( lbf::isExposedBean )
			              .collect( Collectors.toList() )
			              .forEach( lifecycleBeans::remove );
		}

		return lifecycleBeans;
	}
}
