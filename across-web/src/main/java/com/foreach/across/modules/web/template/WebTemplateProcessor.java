package com.foreach.across.modules.web.template;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebTemplateProcessor
{
	/**
	 * Called before the handler is executed.
	 */
	void prepareForTemplate( HttpServletRequest request, HttpServletResponse response, Object handler );

	/**
	 * Called after the handler is executed, before the view is rendered.
	 */
	void applyTemplate( HttpServletRequest request,
	                    HttpServletResponse response,
	                    Object handler,
	                    ModelAndView modelAndView );
}
