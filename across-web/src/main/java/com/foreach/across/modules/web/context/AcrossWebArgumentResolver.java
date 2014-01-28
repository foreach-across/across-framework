package com.foreach.across.modules.web.context;

import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AcrossWebArgumentResolver implements HandlerMethodArgumentResolver
{
	public boolean supportsParameter( MethodParameter parameter ) {
		return parameter.getParameterType().equals( WebResourceRegistry.class );
	}

	public Object resolveArgument( MethodParameter parameter,
	                               ModelAndViewContainer mavContainer,
	                               NativeWebRequest webRequest,
	                               WebDataBinderFactory binderFactory ) throws Exception {
		return WebResourceUtils.getRegistry( webRequest );
	}
}
