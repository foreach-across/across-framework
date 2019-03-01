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

package com.foreach.across.modules.web.resource;

/**
 * Interface used by WebResourceRegistryInterceptor to translate WebResources before rendering.
 *
 * @deprecated since 3.2.0 - will be removed when deprecated {@link WebResourceRegistry} are being removed
 */
@Deprecated
public interface WebResourceTranslator
{
	/**
	 * Verifies if the WebResource matches for this translator.
	 * Calls to shouldTranslate() should never modify the WebResource.
	 *
	 * @param resource WebResource to inspect if it needs to be translated.
	 * @return True if the resource should be modified.
	 */
	boolean shouldTranslate( WebResource resource );

	/**
	 * Modifies the WebResource.
	 *
	 * @param resource WebResource to inspect and modify.
	 */
	void translate( WebResource resource );
}
