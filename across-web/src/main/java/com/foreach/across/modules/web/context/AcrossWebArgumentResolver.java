/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.web.context;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

public class AcrossWebArgumentResolver implements HandlerMethodArgumentResolver
{
	@Autowired
	private MenuFactory menuFactory;

	public boolean supportsParameter( MethodParameter parameter ) {
		Class parameterType = parameter.getParameterType();

		return parameterType.equals( WebResourceRegistry.class )
				|| Menu.class.isAssignableFrom( parameterType )
				|| ViewElementBuilderContext.class.isAssignableFrom( parameterType );
	}

	public Object resolveArgument( MethodParameter parameter,
	                               ModelAndViewContainer mavContainer,
	                               NativeWebRequest webRequest,
	                               WebDataBinderFactory binderFactory ) {

		if ( Menu.class.isAssignableFrom( parameter.getParameterType() ) ) {
			return menuFactory.buildMenu( parameter.getParameterName(),
			                              Menu.class.asSubclass( parameter.getParameterType() ) );
		}

		if ( ViewElementBuilderContext.class.isAssignableFrom( parameter.getParameterType() ) ) {
			return resolveBuilderContext( mavContainer.getModel(), webRequest );
		}

		Optional<WebResourceRegistry> webResourceRegistry = WebResourceUtils.getRegistry( webRequest );
		return webResourceRegistry.isPresent() ? webResourceRegistry.get() : null;
	}

	/**
	 * Resolves the default {@link ViewElementBuilderContext} for the request.
	 * If none is linked to the request, one will be created having the model as parent.
	 *
	 * @param model containing the builder context (and serving as parent)
	 * @return builder context
	 */
	private ViewElementBuilderContext resolveBuilderContext( ModelMap model, WebRequest webRequest ) {
		ViewElementBuilderContext ctx = (ViewElementBuilderContext)
				webRequest.getAttribute( ViewElementBuilderContext.class.getName(), RequestAttributes.SCOPE_REQUEST );

		if ( ctx == null ) {
			ctx = new DefaultViewElementBuilderContext( model );
			webRequest.setAttribute( ViewElementBuilderContext.class.getName(), ctx, RequestAttributes.SCOPE_REQUEST );
		}

		return ctx;
	}
}
