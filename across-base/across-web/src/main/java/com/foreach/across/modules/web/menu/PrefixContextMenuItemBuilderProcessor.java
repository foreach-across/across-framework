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

package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.context.PrefixingPathContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Applies a prefix context to a newly create menu item: applies the prefix to the url
 * and creates an extra matcher for the prefix path.
 */
public class PrefixContextMenuItemBuilderProcessor implements MenuItemBuilderProcessor
{
	private final PrefixingPathContext context;

	public PrefixContextMenuItemBuilderProcessor( PrefixingPathContext context ) {
		this.context = context;
	}

	@Override
	public Menu process( Menu menu ) {
		if ( menu.hasUrl() || !menu.isGroup() ) {
			menu.setUrl( context.path( menu.getUrl() ) );
		}

		Set<String> totalMatchers = new HashSet<>();
		Collection<String> matchers = menu.getAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS );

		if ( matchers != null ) {
			totalMatchers.addAll( matchers );
		}

		totalMatchers.add( context.path( menu.getPath() ) );

		menu.setAttribute( RequestMenuSelector.ATTRIBUTE_MATCHERS, totalMatchers );

		return menu;
	}
}
