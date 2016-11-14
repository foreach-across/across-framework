package com.foreach.across.modules.web.template;

import com.foreach.across.modules.web.resource.WebResourceRegistryInterceptor;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Adds layout logic to the {@link ExceptionHandlerExceptionResolver}.  If an exception occurs, the
 * {@link com.foreach.across.modules.web.resource.WebResourceRegistry} will be reset and - if one is defined -
 * a {@link WebTemplateProcessor} will be detected and reapplied for the exception handler.
 * <p/>
 * This facilitates annotating {@link org.springframework.web.bind.annotation.ExceptionHandler} methods,
 * or their owning {@link org.springframework.stereotype.Controller} or {@link org.springframework.web.bind.annotation.ControllerAdvice},
 * with a {@link Template}.
 * <p/>
 * Note that in some cases, this means {@link WebTemplateInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)}
 * will be invoked twice for the same template. Once by the original request and the second time by this resolver while
 * invoking the applicable {@link org.springframework.web.bind.annotation.ExceptionHandler}.
 *
 * @author Niels Doucet, Arne Vandamme
 * @see LayoutTemplateProcessorAdapterBean
 * @since 1.1.1
 */
public class LayoutSupportingExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver
{
	private ThreadLocal<Optional<ServletInvocableHandlerMethod>> handlerMethodThreadLocal
			= new NamedThreadLocal<>( "ExceptionHandlerMethod" );

	@SuppressWarnings("all")
	private Optional<WebTemplateInterceptor> webTemplateInterceptor = Optional.empty();

	@SuppressWarnings("all")
	private Optional<WebResourceRegistryInterceptor> webResourceRegistryInterceptor = Optional.empty();

	public void setWebTemplateInterceptor( WebTemplateInterceptor webTemplateInterceptor ) {
		this.webTemplateInterceptor = Optional.ofNullable( webTemplateInterceptor );
	}

	public void setWebResourceRegistryInterceptor( WebResourceRegistryInterceptor webResourceRegistryInterceptor ) {
		this.webResourceRegistryInterceptor = Optional.ofNullable( webResourceRegistryInterceptor );
	}

	@Override
	protected ModelAndView doResolveHandlerMethodException( HttpServletRequest request,
	                                                        HttpServletResponse response,
	                                                        HandlerMethod handlerMethod,
	                                                        Exception exception ) {
		try {
			HandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod( handlerMethod, exception );

			webResourceRegistryInterceptor.ifPresent(
					interceptor -> interceptor.preHandle( request, response, exceptionHandlerMethod )
			);
			webTemplateInterceptor.ifPresent(
					interceptor -> interceptor.preHandle( request, response, exceptionHandlerMethod )
			);

			ModelAndView modelAndView
					= super.doResolveHandlerMethodException( request, response, handlerMethod, exception );

			webTemplateInterceptor.ifPresent(
					interceptor -> interceptor.postHandle( request, response, exceptionHandlerMethod, modelAndView )
			);
			webResourceRegistryInterceptor.ifPresent(
					interceptor -> interceptor.postHandle( request, response, exceptionHandlerMethod, modelAndView )
			);

			return modelAndView;
		}
		finally {
			handlerMethodThreadLocal.remove();
		}
	}

	@Override
	protected ServletInvocableHandlerMethod getExceptionHandlerMethod( HandlerMethod handlerMethod,
	                                                                   Exception exception ) {
		Optional<ServletInvocableHandlerMethod> servletInvocableHandlerMethodOptional = handlerMethodThreadLocal.get();

		if ( servletInvocableHandlerMethodOptional == null ) {
			servletInvocableHandlerMethodOptional
					= Optional.ofNullable( super.getExceptionHandlerMethod( handlerMethod, exception ) );
			handlerMethodThreadLocal.set( servletInvocableHandlerMethodOptional );
		}

		return servletInvocableHandlerMethodOptional.isPresent() ? servletInvocableHandlerMethodOptional.get() : null;
	}
}
