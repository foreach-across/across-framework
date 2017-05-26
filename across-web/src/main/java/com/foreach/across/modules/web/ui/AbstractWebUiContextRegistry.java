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

package com.foreach.across.modules.web.ui;

import org.springframework.beans.factory.FactoryBean;

/**
 * <p>
 * The WebUiContextRegistry takes care of request scoping the WebUiContext implementation.
 * This component should be created using a request scope and ScopedProxyMode INTERFACES or TARGET_CLASS.
 * </p>
 * <p>
 * A AbstractWebUiContextHandlerInterceptor must be present to construct the UiContext.
 * </p>
 *
 * @param <T> Specific WebUiContext implementation (use interface).
 */
@Deprecated
public abstract class AbstractWebUiContextRegistry<T extends WebUiContext> implements FactoryBean<T>
{
	private T webUiContext;

	void setWebUiContext( T webUiContext ) {
		this.webUiContext = webUiContext;
	}

	public T getObject() {
		return webUiContext;
	}

	public boolean isSingleton() {
		return true;
	}
}
