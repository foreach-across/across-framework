package com.foreach.across.modules.web.ui;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor that constructs the WebUiContext corresponding to this particular request.
 * Requires an AbstractWebUiContextRegistry to be present in the ApplicationContext.
 *
 * @param <T> Specific WebUiContext implementation (use interface).
 */
public abstract class AbstractWebUiContextHandlerInterceptor<T extends WebUiContext> extends HandlerInterceptorAdapter
{
	@Autowired
	private BeanFactory beanFactory;

	@Autowired(required = false)
	private MessageSource messageSource;

	@SuppressWarnings({"unchecked", "SignatureDeclareThrowsException"})

	@Override
	public boolean preHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		AbstractWebUiContextRegistry registry = beanFactory.getBean( AbstractWebUiContextRegistry.class );

		WebUiContext webUiContext = createWebUiContext( request, response, messageSource );
		registry.setWebUiContext( webUiContext );

		return super.preHandle( request, response, handler );
	}

	protected abstract T createWebUiContext(
			HttpServletRequest request, HttpServletResponse response, MessageSource messageSource );
}
