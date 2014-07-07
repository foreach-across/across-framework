package com.foreach.across.modules.web.template;

import com.foreach.across.core.AcrossException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
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
	                          Object handler ) {
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
	}

	private String determineTemplateName( Object handler ) {
		if ( handler instanceof HandlerMethod ) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;

			if ( handlerMethod.getMethodAnnotation( ClearTemplate.class ) != null || handlerMethod.getMethodAnnotation(
					ResponseBody.class ) != null ) {
				return null;
			}

			Template templateAnnotation = handlerMethod.getMethodAnnotation( Template.class );

			if ( templateAnnotation != null ) {
				return templateAnnotation.value();
			}

			return webTemplateRegistry.getDefaultTemplateName();
		}

		return null;
	}
}
