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

package com.foreach.across.modules.web.mvc;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Extension to the default RequestMappingHandlerAdapter that allows for re-initialization.
 */
public class ReloadableRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter
{
	/**
	 * Reloads the entire configuration.  Only call this method if the properties where not set
	 * using {@link #setArgumentResolvers(java.util.List)}, {@link #setInitBinderArgumentResolvers(java.util.List)}
	 * or {@link #setReturnValueHandlers(java.util.List)}.  If the methods {@link #setCustomArgumentResolvers(java.util.List)},
	 * {@link #setCustomReturnValueHandlers(java.util.List)} were used, calling reload() should not be a problem.
	 */
	public void reload() {
		setArgumentResolvers( null );
		setInitBinderArgumentResolvers( null );
		setReturnValueHandlers( null );

		afterPropertiesSet();
	}
}
