package com.foreach.across.modules.debugweb.mvc;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.DebugWebModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class DebugHandlerMapping extends RequestMappingHandlerMapping
{
	@Autowired
	private DebugWebModule debugWebModule;

	@Override
	protected boolean isHandler( Class<?> beanType ) {
		return AnnotationUtils.findAnnotation( beanType, DebugWebController.class ) != null;
	}

	@Override
	protected RequestMappingInfo getMappingForMethod( Method method, Class<?> handlerType ) {
		RequestMappingInfo info = super.getMappingForMethod( method, handlerType );

		if ( info != null ) {
			DebugWebController annotation = AnnotationUtils.findAnnotation( handlerType, DebugWebController.class );

			// Use the parent path
			if ( !StringUtils.isEmpty( annotation.path() ) ) {
				RequestMappingInfo other = new RequestMappingInfo( new PatternsRequestCondition( annotation.path() ),
				                                                   new RequestMethodsRequestCondition(),
				                                                   new ParamsRequestCondition(),
				                                                   new HeadersRequestCondition(),
				                                                   new ConsumesRequestCondition(),
				                                                   new ProducesRequestCondition(), null );

				info = other.combine( info );
			}

			// Add the subpath
			RequestMappingInfo other =
					new RequestMappingInfo( new PatternsRequestCondition( debugWebModule.getRootPath() ),
					                        new RequestMethodsRequestCondition(), new ParamsRequestCondition(),
					                        new HeadersRequestCondition(), new ConsumesRequestCondition(),
					                        new ProducesRequestCondition(), null );

			info = other.combine( info );
		}

		return info;
	}
}
