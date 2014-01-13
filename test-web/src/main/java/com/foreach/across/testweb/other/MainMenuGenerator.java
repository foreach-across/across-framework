package com.foreach.across.testweb.other;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuConstructEvent;
import com.foreach.across.testweb.SpecificUiContext;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@AcrossEventHandler
public class MainMenuGenerator
{
	@Autowired
	private SpecificUiContext uiContext;

	@Handler
	public void onApplicationEvent( MenuConstructEvent menuConstructEvent ) {

		Menu menu = menuConstructEvent.getMenu();

		menu.addItem( "item " + System.currentTimeMillis() );
		//menu.addItem( uiContext.getName() );
		menu.addItem( uiContext.toString() );
		menu.addItem( uiContext.getRequest().toString() );
		menu.addItem( uiContext.getResponse().toString() );
	}
}
