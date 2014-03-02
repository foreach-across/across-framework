package com.foreach.across.modules.web.mvc;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Scans matching beans for RequestMapping annotations and (optionally) prefixes all mappings.
 */
public class PrefixingRequestMappingHandlerMapping extends RequestMappingHandlerMapping
{
	private final String prefixPath;
	private final ClassFilter handlerMatcher;

	private ApplicationContext contextBeingScanned;

	public PrefixingRequestMappingHandlerMapping( String prefixPath, ClassFilter handlerMatcher ) {
		this.prefixPath = prefixPath;
		this.handlerMatcher = handlerMatcher;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	@Override
	protected void initHandlerMethods() {
	}

	/**
	 * Scan a particular ApplicationContext for DebugWebController instances.
	 *
	 * @param context
	 */
	public synchronized void scan( ApplicationContext context ) {
		contextBeingScanned = context;

		if ( logger.isDebugEnabled() ) {
			logger.debug( "Looking for request mappings in application context: " + context );
		}

		String[] beanNames = context.getBeanNamesForType( Object.class );

		for ( String beanName : beanNames ) {
			if ( isHandler( context.getType( beanName ) ) ) {
				detectHandlerMethods( context, beanName );
			}
		}

		contextBeingScanned = null;

		handlerMethodsInitialized( getHandlerMethods() );
	}

	protected void detectHandlerMethods( ApplicationContext context, final Object handler ) {
		Class<?> handlerType = ( handler instanceof String ) ? context.getType( (String) handler ) : handler.getClass();

		final Class<?> userType = ClassUtils.getUserClass( handlerType );

		Set<Method> methods = HandlerMethodSelector.selectMethods( userType, new ReflectionUtils.MethodFilter()
		{
			public boolean matches( Method method ) {
				return getMappingForMethod( method, userType ) != null;
			}
		} );

		for ( Method method : methods ) {
			RequestMappingInfo mapping = getMappingForMethod( method, userType );
			registerHandlerMethod( handler, method, mapping );
		}
	}

	@Override
	protected HandlerMethod createHandlerMethod( Object handler, Method method ) {
		HandlerMethod handlerMethod;
		if ( handler instanceof String ) {
			String beanName = (String) handler;
			handlerMethod = new HandlerMethod( beanName, contextBeingScanned, method );
		}
		else {
			handlerMethod = new HandlerMethod( handler, method );
		}
		return handlerMethod;
	}

	@Override
	protected boolean isHandler( Class<?> beanType ) {
		return handlerMatcher.matches( beanType );
	}

	@Override
	protected RequestMappingInfo getMappingForMethod( Method method, Class<?> handlerType ) {
		RequestMappingInfo info = super.getMappingForMethod( method, handlerType );

		if ( info != null ) {
			/*
			DebugWebController annotation = AnnotationUtils.findAnnotation( handlerType, DebugWebController.class );

			// Use the parent path
			if ( !StringUtils.isEmpty( annotation.path() ) ) {
				RequestMappingInfo other = new RequestMappingInfo( new PatternsRequestCondition( annotation.path() ),
				                                                   new RequestMethodsRequestCondition(),
				                                                   new ParamsRequestCondition(),
				                                                   new HeadersRequestCondition(),
				                                                   new ConsumesRequestCondition(),
				                                                   new ProducesRequestCondition(), null );

				info = other.combine( info );
			}*/

			// Add the subpath
			RequestMappingInfo other = new RequestMappingInfo( new PatternsRequestCondition( prefixPath ),
			                                                   new RequestMethodsRequestCondition(),
			                                                   new ParamsRequestCondition(),
			                                                   new HeadersRequestCondition(),
			                                                   new ConsumesRequestCondition(),
			                                                   new ProducesRequestCondition(), null );

			info = other.combine( info );
		}

		return info;
	}
}
