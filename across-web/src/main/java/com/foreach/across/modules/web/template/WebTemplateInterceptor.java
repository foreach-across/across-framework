package com.foreach.across.modules.web.template;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Finds and applies the web template configured to a particular request.
 */
public class WebTemplateInterceptor extends HandlerInterceptorAdapter
{
	public static final String PROCESSOR_ATTRIBUTE = WebTemplateProcessor.class.toString();

	private final WebTemplateRegistry webTemplateRegistry;

	public WebTemplateInterceptor( WebTemplateRegistry webTemplateRegistry ) {
		this.webTemplateRegistry = webTemplateRegistry;
	}

	@Override
	public boolean preHandle( HttpServletRequest request,
	                          HttpServletResponse response,
	                          Object handler ) throws Exception {

		String templateName = determineTemplateName( handler );
		WebTemplateProcessor processor = webTemplateRegistry.get( templateName );

		if ( processor != null ) {
			request.setAttribute( PROCESSOR_ATTRIBUTE, processor );
			processor.prepareForTemplate( request, response, handler );
		}

		return true;
	}

	@Override
	public void postHandle( HttpServletRequest request,
	                        HttpServletResponse response,
	                        Object handler,
	                        ModelAndView modelAndView ) throws Exception {
		WebTemplateProcessor processor = (WebTemplateProcessor) request.getAttribute( PROCESSOR_ATTRIBUTE );

		if ( processor != null ) {
			processor.applyTemplate( request, response, handler, modelAndView );
		}
	}

	private String determineTemplateName( Object handler ) {
		return webTemplateRegistry.getDefaultTemplateName();
	}
}
