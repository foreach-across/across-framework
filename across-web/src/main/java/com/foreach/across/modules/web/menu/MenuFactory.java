package com.foreach.across.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuConstructEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MenuFactory
{
	@Autowired
	private ApplicationContext applicationContext;

	public Menu generate( String name ) {
		Menu menu = new Menu( name );

		MenuConstructEvent e = new MenuConstructEvent( this, menu );
		applicationContext.publishEvent( e );

		return menu;
	}
}
