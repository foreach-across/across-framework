package com.foreach.across.modules.debugweb.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class DebugPageViewArgumentResolver implements HandlerMethodArgumentResolver
{
	@Autowired
	private DebugPageViewFactory debugPageViewFactory;

	public boolean supportsParameter( MethodParameter parameter ) {
		return parameter.getParameterType() == DebugPageView.class;
	}

	public Object resolveArgument( MethodParameter parameter,
	                               ModelAndViewContainer mavContainer,
	                               NativeWebRequest webRequest,
	                               WebDataBinderFactory binderFactory ) throws Exception {

		return debugPageViewFactory.buildView();
	}
}
