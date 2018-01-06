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
package com.foreach.across.modules.web.error;

import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.DefaultErrorViewResolver;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default error view resolver that will detect the presence of Thymeleaf templates
 * in a module location: eg: *MODULE_RESOURCES/error/5xx.html*.
 * Automatically injected by AcrossWebModule in any dynamic application module.
 *
 * @author Arne Vandamme
 * @see DefaultErrorViewResolver
 * @since 3.0.0
 */
@ConditionalOnBean(SpringTemplateEngine.class)
public class AcrossModuleDefaultErrorViewResolver implements ErrorViewResolver, Ordered
{
	private static final Map<HttpStatus.Series, String> SERIES_VIEWS;

	static {
		Map<HttpStatus.Series, String> views = new HashMap<HttpStatus.Series, String>();
		views.put( HttpStatus.Series.CLIENT_ERROR, "4xx" );
		views.put( HttpStatus.Series.SERVER_ERROR, "5xx" );
		SERIES_VIEWS = Collections.unmodifiableMap( views );
	}

	private final AcrossModuleInfo moduleInfo;
	private final ResourceLoader resourceLoader;

	private int order = Ordered.LOWEST_PRECEDENCE;

	private AcrossModuleDefaultErrorViewResolver( AcrossModuleInfo moduleInfo, ResourceLoader resourceLoader ) {
		this.moduleInfo = moduleInfo;
		this.resourceLoader = resourceLoader;
	}

	@Override
	public ModelAndView resolveErrorView( HttpServletRequest request, HttpStatus status,
	                                      Map<String, Object> model ) {
		ModelAndView modelAndView = resolve( String.valueOf( status ), model );
		if ( modelAndView == null && SERIES_VIEWS.containsKey( status.series() ) ) {
			modelAndView = resolve( SERIES_VIEWS.get( status.series() ), model );
		}
		return modelAndView;
	}

	private ModelAndView resolve( String viewName, Map<String, Object> model ) {
		String errorViewName = "th/" + moduleInfo.getResourcesKey() + "/error/" + viewName;
		String templateLocation = "classpath:/views/" + errorViewName + ".html";

		if ( resourceLoader.getResource( templateLocation ).exists() ) {
			return new ModelAndView( errorViewName, model );
		}

		return null;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public void setOrder( int order ) {
		this.order = order;
	}
}
