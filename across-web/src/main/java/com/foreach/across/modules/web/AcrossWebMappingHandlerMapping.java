package com.foreach.across.modules.web;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class AcrossWebMappingHandlerMapping extends RequestMappingHandlerMapping
{
	public void scan( ApplicationContext context ) {
		setApplicationContext( context );

		initHandlerMethods();
	}
}
