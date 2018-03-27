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

import com.foreach.across.modules.web.context.WebAppLinkBuilder;

/**
 * Fallback link builder that does not actually transform the link but simply returns the original.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
class FallbackWebAppLinkBuilder implements WebAppLinkBuilder
{
	@Override
	public String buildLink( String baseLink ) {
		return baseLink;
	}

	@Override
	public String buildLink( String baseLink, boolean encodeUrl ) {
		return baseLink;
	}
}
