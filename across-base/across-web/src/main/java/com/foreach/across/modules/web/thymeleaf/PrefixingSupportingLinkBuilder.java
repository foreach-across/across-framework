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
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.context.WebAppPathResolver;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;

import java.util.Collections;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
public final class PrefixingSupportingLinkBuilder extends StandardLinkBuilder
{
	private final WebAppPathResolver pathResolver;

	public PrefixingSupportingLinkBuilder( WebAppPathResolver pathResolver ) {
		this.pathResolver = pathResolver;
	}

	@Override
	protected String processLink( IExpressionContext context, String link ) {
		if ( link.length() > 0 && link.charAt( 0 ) == '@' ) {
			String prefixed = pathResolver.path( link );
			if ( prefixed.length() > 0 && prefixed.charAt( 0 ) == '/' ) {
				if ( prefixed.length() > 1 && prefixed.charAt( 1 ) != '/' ) {
					String contextPath = computeContextPath( context, prefixed, Collections.emptyMap() );
					if ( contextPath != null && contextPath.length() > 0 && !"/".equals( contextPath ) ) {
						return super.processLink( context, contextPath + prefixed );
					}
				}
			}

			return super.processLink( context, prefixed );
		}

		return super.processLink( context, link );
	}
}
