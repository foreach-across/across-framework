package com.foreach.across.modules.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;

@Configuration
public class AcrossWebConfig
{

/*
	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		ControllerOnlyRequestMappingHandlerMapping mapping = new ControllerOnlyRequestMappingHandlerMapping();
		mapping.setOrder( 0 );
//		mapping.setInterceptors( getInterceptors() );

		return mapping;
	}
	*/
}
