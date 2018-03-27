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

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * A MenuSelector is a strategy interface that searches a Menu tree and returns at most one Menu item that matches it.
 */
public interface MenuSelector
{
	/**
	 * Create a {@code MenuSelector} that will look for the item best matching the {@link HttpServletRequest}.
	 *
	 * @param request to match with
	 * @return selector
	 * @see RequestMenuSelector
	 */
	static MenuSelector byHttpServletRequest( @NonNull HttpServletRequest request ) {
		return new RequestMenuSelector( request );
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given path.
	 *
	 * @param path Path to look for.
	 * @return MenuSelector instance.
	 */
	static MenuSelector byPath( final String path ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( path, item.getPath() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given url.
	 *
	 * @param url URL to look for.
	 * @return MenuSelector instance.
	 */
	static MenuSelector byUrl( final String url ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( url, item.getUrl() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given name.
	 *
	 * @param name Name to look for.
	 * @return MenuSelector instance.
	 */
	static MenuSelector byName( final String name ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( name, item.getName() );
			}
		};
	}

	/**
	 * Creates a MenuSelector that will look for the MenuItem with the given title.
	 *
	 * @param title Title to look for.
	 * @return MenuSelector instance.
	 */
	static MenuSelector byTitle( final String title ) {
		return new TraversingMenuSelector( false )
		{
			@Override
			protected boolean matches( Menu item ) {
				return StringUtils.equals( title, item.getTitle() );
			}
		};
	}

	/**
	 * Search the given Menu for an item that matches the current selector.
	 *
	 * @param menu Menu tree to search top-down.
	 * @return Matching Menu item or null if not found.
	 */
	Menu find( Menu menu );
}
