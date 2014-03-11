package com.foreach.across.modules.web.mvc;

import org.springframework.web.accept.ContentNegotiationManager;

import javax.servlet.ServletContext;

public class ContentNegotiationConfigurer extends org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
{
	public ContentNegotiationConfigurer( ServletContext servletContext ) {
		super( servletContext );
	}

	@Override
	public ContentNegotiationManager getContentNegotiationManager() throws Exception {
		return super.getContentNegotiationManager();
	}
}
