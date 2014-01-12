package com.foreach.across.testweb.sub;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuConstructEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ExtensionMenuGenerator implements ApplicationListener<MenuConstructEvent>
{
	public void onApplicationEvent( MenuConstructEvent menuConstructEvent ) {
		Menu menu = menuConstructEvent.getMenu();
		menu.addItem( "item 3" );
	}
}
