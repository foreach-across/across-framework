package com.foreach.across.modules.web.mvc;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("all")
public class ResourceHandlerRegistry extends org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
{
	public ResourceHandlerRegistry( ApplicationContext applicationContext, ServletContext servletContext ) {
		super( applicationContext, servletContext );
	}

	@Override
	public AbstractHandlerMapping getHandlerMapping() {
		return super.getHandlerMapping();
	}

	public Map<String, ?> getUrlMap() {
		AbstractHandlerMapping mapping = getHandlerMapping();

		if ( mapping instanceof SimpleUrlHandlerMapping ) {
			return ( (SimpleUrlHandlerMapping) mapping ).getUrlMap();
		}

		return Collections.emptyMap();
	}
}
