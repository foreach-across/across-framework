package com.foreach.across.testweb.sub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletContext;

@Configuration
public class TestWebWebConfig extends WebMvcConfigurationSupport
{
	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping mapping = super.requestMappingHandlerMapping();
		mapping.setDetectHandlerMethodsInAncestorContexts( true );

		return mapping;
	}
}
