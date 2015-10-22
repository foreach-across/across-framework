package com.foreach.across.modules.web.template;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds layouting logic to the {@link ExceptionHandlerExceptionResolver}.
 * This facilitates annotating {@link org.springframework.web.bind.annotation.ExceptionHandler} methods,
 * or their containing {@link org.springframework.stereotype.Controller}, with a {@link Template}.
 * <p/>
 * Note that {@link WebTemplateInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)} will be invoked twice.
 * Once by the original request and the second time by this resolver while invoking the applicable {@link org.springframework.web.bind.annotation.ExceptionHandler}.
 *
 * @author niels
 * @since 9/09/2015
 * @see LayoutTemplateProcessorAdapterBean
 */
public class LayoutingExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver
{

	private final WebTemplateInterceptor webTemplateInterceptor;

	public LayoutingExceptionHandlerExceptionResolver( WebTemplateInterceptor webTemplateInterceptor ) {
		super();
		this.webTemplateInterceptor = webTemplateInterceptor;
	}

	@Override
	protected ModelAndView doResolveHandlerMethodException( HttpServletRequest request,
	                                                        HttpServletResponse response,
	                                                        HandlerMethod handlerMethod,
	                                                        Exception exception ) {
		webTemplateInterceptor.preHandle( request, response, handlerMethod );
		ModelAndView modelAndView = super.doResolveHandlerMethodException( request, response, handlerMethod,
		                                                                   exception );
		webTemplateInterceptor.postHandle( request, response, handlerMethod, modelAndView );
		return modelAndView;
	}
}
