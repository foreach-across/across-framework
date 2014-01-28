package com.foreach.across.modules.web.menu;

import com.foreach.across.core.events.AcrossEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuFactory
{
	@Autowired
	private AcrossEventPublisher publisher;

	public Menu buildMenu( String name ) {
		Menu menu = new Menu( name );

		return buildMenu( menu );
	}

	public <T extends Menu> T buildMenu( T menu ) {
		BuildMenuEvent<T> e = new BuildMenuEvent<T>( this, menu );
		publisher.publish( e );

		// Always sort a menu after the initial build
		menu.sort();

		return menu;
	}
}
