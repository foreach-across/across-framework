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

package com.foreach.across.modules.web.events;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Filter that can easily be extended to provide filtering BuildMenuEvents on the menu name.
 */
public class AbstractMenuFilter implements IMessageFilter
{
	private final String menuName;

	protected AbstractMenuFilter( String menuName ) {
		this.menuName = menuName;
	}

	public boolean accepts( Object message, MessageHandler metadata ) {
		return message instanceof BuildMenuEvent && StringUtils.equals( ( (BuildMenuEvent) message ).getMenuName(),
		                                                                menuName );
	}
}
