package com.foreach.across.testweb.other;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuConstructEvent;
import com.foreach.across.testweb.SpecificUiContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MainMenuGenerator implements ApplicationListener<MenuConstructEvent>
{
	@Autowired
	private SpecificUiContext uiContext;

	public void onApplicationEvent( MenuConstructEvent menuConstructEvent ) {

		Menu menu = menuConstructEvent.getMenu();

		menu.addItem( "item " + System.currentTimeMillis() );
		//menu.addItem( uiContext.getName() );
		menu.addItem( uiContext.toString() );
		menu.addItem( uiContext.getRequest().toString() );
		menu.addItem( uiContext.getResponse().toString() );
	}
}
