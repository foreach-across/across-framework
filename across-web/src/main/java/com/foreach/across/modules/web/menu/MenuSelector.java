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

/**
 * A MenuSelector is a strategy interface that searches a Menu tree and returns at most one Menu item that matches it.
 */
public interface MenuSelector
{
	/**
	 * Search the given Menu for an item that matches the current selector.
	 *
	 * @param menu Menu tree to search top-down.
	 * @return Matching Menu item or null if not found.
	 */
	Menu find( Menu menu );
}
