package com.foreach.across.modules.web.context;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AcrossWebArgumentResolver implements HandlerMethodArgumentResolver
{
	@Autowired
	private MenuFactory menuFactory;

	public boolean supportsParameter( MethodParameter parameter ) {
		Class parameterType = parameter.getParameterType();

		return parameterType.equals( WebResourceRegistry.class ) || Menu.class.isAssignableFrom( parameterType );
	}

	public Object resolveArgument( MethodParameter parameter,
	                               ModelAndViewContainer mavContainer,
	                               NativeWebRequest webRequest,
	                               WebDataBinderFactory binderFactory ) throws Exception {

		if ( Menu.class.isAssignableFrom( parameter.getParameterType() ) ) {
			return menuFactory.buildMenu( parameter.getParameterName(), (Class<? extends Menu>) parameter.getParameterType() );
		}

		return WebResourceUtils.getRegistry( webRequest );
	}
}
