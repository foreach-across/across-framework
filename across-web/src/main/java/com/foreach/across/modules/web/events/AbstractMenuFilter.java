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