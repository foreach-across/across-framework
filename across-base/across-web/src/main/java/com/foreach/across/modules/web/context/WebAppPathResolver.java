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
package com.foreach.across.modules.web.context;

/**
 * Interface for resolving paths within the current webapp.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.context.PrefixingPathContext
 */
public interface WebAppPathResolver
{
	/**
	 * Resolves the requested path in the relative context of the web application.
	 * <b>Note</b> that this does not take the actual web application context of the container into account.
	 *
	 * @param path Original path to be resolved.
	 * @return Resolved path - might be modified.
	 */
	String path( String path );

	/**
	 * Creates a Spring MVC redirect for the requested path.  This will usually apply the regular
	 * path resolving and prepend redirect: if necessary.
	 *
	 * @param path Original path for which to create a redirect.
	 * @return Redirect path.
	 */
	String redirect( String path );
}
