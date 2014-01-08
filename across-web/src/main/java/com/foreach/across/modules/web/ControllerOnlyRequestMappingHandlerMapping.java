package com.foreach.across.modules.web;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class ControllerOnlyRequestMappingHandlerMapping extends RequestMappingHandlerMapping
{
	@Override
	protected boolean isHandler( Class<?> beanType ) {
		return ( ( AnnotationUtils.findAnnotation( beanType, Controller.class ) != null ) );
	}
}
