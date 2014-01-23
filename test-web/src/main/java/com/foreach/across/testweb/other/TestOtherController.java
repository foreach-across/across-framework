package com.foreach.across.testweb.other;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.ui.WebUiContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Refreshable
public class TestOtherController
{
	@Autowired(required = false)
	private WebUiContext context;

	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private HelperBean helper;

	public TestOtherController() {
		System.out.println( "other controller created" );
	}

	@RequestMapping("/other")
	public ModelAndView hello() {
		//System.out.println( context.getName() );
		System.out.println( helper.getHelloFromHelper() );
		return new ModelAndView( "other/test" ).addObject( "menu", menuFactory.buildMenu( "mainMenu" ) );
	}
}
