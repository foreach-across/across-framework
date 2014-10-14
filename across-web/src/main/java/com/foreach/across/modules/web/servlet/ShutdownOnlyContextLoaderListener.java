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

package com.foreach.across.modules.web.servlet;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;

/**
 * Only supports the context destroyed ServletContextEvent, as it assumes
 * that the context has already been initialized when passed to the listener.
 *
 * @author Arne Vandamme
 */
class ShutdownOnlyContextLoaderListener extends ContextLoaderListener
{
	public ShutdownOnlyContextLoaderListener( WebApplicationContext context ) {
		super( context );
	}

	@Override
	public void contextInitialized( ServletContextEvent event ) {
		// do nothing
	}
}
