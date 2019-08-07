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

/**
 * Holds the Spring ApplicationContext information for the AcrossContext or an AcrossModule.
 */
public class AcrossApplicationContextHolder
{
	private final AcrossConfigurableApplicationContext applicationContext;
	private final AcrossApplicationContextHolder parent;

	public AcrossApplicationContextHolder( AcrossConfigurableApplicationContext applicationContext ) {
		this( applicationContext, null );
	}

	public AcrossApplicationContextHolder( AcrossConfigurableApplicationContext applicationContext,
	                                       AcrossApplicationContextHolder parent ) {
		this.applicationContext = applicationContext;
		this.parent = parent;
	}

	public AcrossConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public AcrossListableBeanFactory getBeanFactory() {
		return (AcrossListableBeanFactory) applicationContext.getBeanFactory();
	}

	public AcrossApplicationContextHolder getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}
}
