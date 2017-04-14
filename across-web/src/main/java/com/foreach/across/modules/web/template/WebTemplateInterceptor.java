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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Finds and applies the web template configured to a particular request.
 * Also provides support for partial rendering, though this assumes Thymeleaf templates.
 * <p/>
 * <strong>Partial rendering</strong><br />
 * When a <strong>_partial</strong> parameter is present it will be parsed for a fragment and view element specification.
 * The partial parameter can be of the following forms:
 * <ul>
 * <li><strong>FRAGMENT</strong>: only the template fragment that should be rendered</li>
 * <li><strong>::VIEW_ELEMENT_NAME</strong>: only the name of the view element that should be rendered</li>
 * <li><strong>FRAGMENT::VIEW_ELEMENT_NAME</strong>: both template fragment and name of the view element that should be rendered</li>
 * </ul>
 * <p/>
 * In case a template fragment is specified, the web template itself will not be applied and the fragment will be automatically appended
 * to the view returned by the controller.
 * <p/>
 * In case a ViewElement name is specified, only the output of ViewElements with that name will be returned.
 * You must ensure that the name is unique if you only want a single element rendered.
 *
 * @see com.foreach.across.modules.web.template.Template
 * @see com.foreach.across.modules.web.template.ClearTemplate
 */
public class WebTemplateInterceptor extends HandlerInterceptorAdapter
{
	public static final String PROCESSOR_ATTRIBUTE = WebTemplateProcessor.class.toString();
	public static final String PARTIAL_PARAMETER = "_partial";

	public static final String RENDER_FRAGMENT = "_partial.fragment";
	public static final String RENDER_VIEW_ELEMENT = "_partial.viewElement";

	private final WebTemplateRegistry webTemplateRegistry;

	public WebTemplateInterceptor( WebTemplateRegistry webTemplateRegistry ) {
		this.webTemplateRegistry = webTemplateRegistry;
	}

	@Override
	public boolean preHandle( HttpServletRequest request,
	                          HttpServletResponse response,
	                          Object handler ) {
		Optional<String> partial = Optional.ofNullable( request.getParameter( PARTIAL_PARAMETER ) );

		// if the request contains a parameter _partial, then we will not render a template
		boolean hasFragment = false;

		if ( partial.isPresent() ) {
			hasFragment = parsePartialParametersAndReturnTrueIfFragment( request, partial.get() );
		}

		if ( !hasFragment ) {
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
			else {
				// remove any existing template processor - in case of exception handling
				request.removeAttribute( PROCESSOR_ATTRIBUTE );
			}
		}

		return true;
	}

	private boolean parsePartialParametersAndReturnTrueIfFragment( HttpServletRequest request, String partial ) {
		if ( !StringUtils.isBlank( partial ) ) {
			request.setAttribute( PARTIAL_PARAMETER, partial );

			int position = StringUtils.indexOf( partial, "::" );

			if ( position == 0 ) {
				request.setAttribute( RENDER_VIEW_ELEMENT, partial.substring( 2 ) );
				return false;
			}
			else if ( position > 0 ) {
				request.setAttribute( RENDER_FRAGMENT, partial.substring( 0, position ) );
				request.setAttribute( RENDER_VIEW_ELEMENT, partial.substring( position + 2 ) );
				return true;
			}
			else {
				request.setAttribute( RENDER_FRAGMENT, partial );
				return true;
			}
		}

		return false;
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
		else if ( modelAndView != null && modelAndView.hasView() && modelAndView.isReference() ) {
			Optional.ofNullable( request.getAttribute( RENDER_FRAGMENT ) )
			        .ifPresent( fragment -> {
				        String viewName = modelAndView.getViewName();

				        if (
					        // Redirect views should not be modified
						        !StringUtils.startsWithAny( viewName, UrlBasedViewResolver.REDIRECT_URL_PREFIX,
						                                    UrlBasedViewResolver.FORWARD_URL_PREFIX )
								        // Nor should views that already contain a fragment
								        && !viewName.contains( "::" )
						        ) {
					        modelAndView.setViewName( viewName + "::" + fragment );
				        }
			        } );
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
