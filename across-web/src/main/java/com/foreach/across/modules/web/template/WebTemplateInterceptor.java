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

package com.foreach.across.modules.web.template;

import com.foreach.across.core.AcrossException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Callable;

/**
 * Finds and applies the web template configured to a particular request.
 *
 * @see com.foreach.across.modules.web.template.Template
 * @see com.foreach.across.modules.web.template.ClearTemplate
 */
public class WebTemplateInterceptor extends HandlerInterceptorAdapter
{
	public static final String PROCESSOR_ATTRIBUTE = WebTemplateProcessor.class.toString();
	public static final String PARTIAL_PARAMETER = "_partial";

	private final WebTemplateRegistry webTemplateRegistry;

	public WebTemplateInterceptor( WebTemplateRegistry webTemplateRegistry ) {
		this.webTemplateRegistry = webTemplateRegistry;
	}

	@Override
	public boolean preHandle( HttpServletRequest request,
	                          HttpServletResponse response,
	                          Object handler ) {
		boolean containsPartialParameter = request.getParameterMap().containsKey( PARTIAL_PARAMETER );

		// if the request contains a parameter _partial, then we will not render a template
		if ( !containsPartialParameter ) {
			String templateName = determineTemplateName( handler );

			if ( templateName != null ) {
				WebTemplateProcessor processor = webTemplateRegistry.get( templateName );

				if ( processor != null ) {
					request.setAttribute( PROCESSOR_ATTRIBUTE, processor );
					processor.prepareForTemplate( request, response, handler );
				}
				else {
					throw new AcrossException( "No WebTemplateProcessor registered with name: " + templateName );
				}
			}
		}

		return true;
	}

	@Override
	public void postHandle( HttpServletRequest request,
	                        HttpServletResponse response,
	                        Object handler,
	                        ModelAndView modelAndView ) {
		WebTemplateProcessor processor = (WebTemplateProcessor) request.getAttribute( PROCESSOR_ATTRIBUTE );

		if ( processor != null ) {
			processor.applyTemplate( request, response, handler, modelAndView );
		}
		boolean containsPartialParameter = request.getParametermap().containsKey( PARTIAL_PARAMETER );
		if ( containsPartialParameter ) {
			modelAndView.setViewName( modelAndView.getViewName() + "::" + request.getParameter( PARTIAL_PARAMETER ) );
		}
	}

	private String determineTemplateName( Object handler ) {
		if ( handler instanceof HandlerMethod ) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;

			if ( supportsTemplate( handlerMethod ) ) {
				Template templateAnnotation = handlerMethod.getMethodAnnotation( Template.class );

				if ( templateAnnotation == null ) {
					// Get template from the controller
					Class<?> controllerClass = handlerMethod.getBeanType();
					templateAnnotation = AnnotationUtils.findAnnotation( controllerClass, Template.class );
				}

				if ( templateAnnotation != null ) {
					return templateAnnotation.value();
				}

				return webTemplateRegistry.getDefaultTemplateName();
			}
		}

		return null;
	}

	private boolean supportsTemplate( HandlerMethod handlerMethod ) {
		// Clearing template on method trumps all
		if ( handlerMethod.getMethodAnnotation( ClearTemplate.class ) != null ) {
			return false;
		}

		// If the handler method has an annotation directly,
		// we ignore default settings but assume template handling is wanted
		if ( handlerMethod.getMethodAnnotation( Template.class ) != null ) {
			return true;
		}

		Class<?> controllerClass = handlerMethod.getBeanType();

		// ResponseBody methods don't support templates,
		// clearing template on controller without overriding on method has same effect
		if ( handlerMethod.getMethodAnnotation( ResponseBody.class ) != null
				|| AnnotationUtils.findAnnotation( controllerClass, ResponseBody.class ) != null
				|| AnnotationUtils.findAnnotation( controllerClass, ClearTemplate.class ) != null ) {
			return false;
		}

		if ( !handlerMethod.isVoid() ) {
			Class<?> returnType = handlerMethod.getReturnType().getParameterType();

			// HttpEntity and HttpHeaders don't support templates
			if ( HttpEntity.class.isAssignableFrom( returnType ) || HttpHeaders.class.isAssignableFrom( returnType ) ) {
				return false;
			}

			// Asynchronous calls are not supported either
			if ( Callable.class.isAssignableFrom( returnType )
					|| DeferredResult.class.isAssignableFrom( returnType )
					|| ListenableFuture.class.isAssignableFrom( returnType ) ) {
				return false;
			}
		}

		return true;
	}
}
