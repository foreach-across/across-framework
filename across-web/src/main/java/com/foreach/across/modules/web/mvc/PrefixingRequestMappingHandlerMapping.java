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

package com.foreach.across.modules.web.mvc;

import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.ClassFilter;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Scans matching beans for RequestMapping annotations and (optionally) prefixes all mappings.
 * Allows for reloading (re-scanning) of mappings and re-initialization of the entire mapping handler mapping.
 * <p>
 * <b>WARN: interceptors are only supported once.</b>
 */
public class PrefixingRequestMappingHandlerMapping extends RequestMappingHandlerMapping
{
	private final String prefixPath;
	private final ClassFilter handlerMatcher;

	private ApplicationContext contextBeingScanned;

	private final Set<Object> scannedHandlers = new HashSet<>();

	public PrefixingRequestMappingHandlerMapping( ClassFilter handlerMatcher ) {
		this.prefixPath = null;
		this.handlerMatcher = handlerMatcher;
	}

	public PrefixingRequestMappingHandlerMapping( String prefixPath, ClassFilter handlerMatcher ) {
		this.prefixPath = prefixPath.endsWith( "/" ) ? StringUtils.stripEnd( prefixPath, "/" ) : prefixPath;
		this.handlerMatcher = handlerMatcher;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	@Override
	protected void initHandlerMethods() {
	}

	/**
	 * Add one ore more interceptors to the handler mapping.
	 *
	 * @param interceptor Interceptors to add.
	 */
	public void addInterceptor( Object... interceptor ) {
		setInterceptors( interceptor );
	}

	@Event
	protected void rescan( AcrossContextBootstrappedEvent event ) {
		for ( AcrossModuleInfo moduleInfo : event.getModules() ) {
			scan( moduleInfo.getApplicationContext(), false );
		}
		scan( event.getContext().getApplicationContext(), true );
	}

	public void reload() {
		initApplicationContext();
	}

	@Override
	protected void detectMappedInterceptors( List<HandlerInterceptor> mappedInterceptors ) {
		for ( HandlerInterceptor mappedInterceptor : BeanFactoryUtils.beansOfTypeIncludingAncestors(
				getApplicationContext(), MappedInterceptor.class, true, false ).values() ) {

			if ( !mappedInterceptors.contains( mappedInterceptor ) ) {
				mappedInterceptors.add( mappedInterceptor );
			}
		}
	}

	/**
	 * Scan a particular ApplicationContext for instances.
	 *
	 * @param context          that should be scanned
	 * @param includeAncestors should controllers from the parent application context be detected
	 */
	public synchronized void scan( ApplicationContext context, boolean includeAncestors ) {
		if ( context == null ) {
			return;
		}

		contextBeingScanned = context;

		if ( logger.isDebugEnabled() ) {
			logger.debug( "Looking for request mappings in application context: " + context );
		}

		String[] beanNames = includeAncestors
				? BeanFactoryUtils.beanNamesIncludingAncestors( context )
				: context.getBeanNamesForType( Object.class );

		for ( String beanName : beanNames ) {
			if ( isHandler( context.getType( beanName ) ) && !scannedHandlers.contains( beanName ) ) {
				scannedHandlers.add( beanName );
				detectHandlerMethods( context, beanName );
			}
		}

		contextBeingScanned = null;

		handlerMethodsInitialized( getHandlerMethods() );
	}

	protected void detectHandlerMethods( ApplicationContext context, final Object handler ) {
		Class<?> handlerType = ( handler instanceof String ) ? context.getType( (String) handler ) : handler.getClass();

		final Class<?> userType = ClassUtils.getUserClass( handlerType );

		Set<Method> methods = MethodIntrospector.selectMethods( userType, new ReflectionUtils.MethodFilter()
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
		return handlerMatcher.matches( ClassUtils.getUserClass( beanType ) );
	}

	@Override
	protected RequestMappingInfo getMappingForMethod( Method method, Class<?> handlerType ) {
		RequestMappingInfo info = super.getMappingForMethod( method, handlerType );

		if ( info != null && prefixPath != null ) {
			RequestMappingInfo other = new RequestMappingInfo( new PatternsRequestCondition( prefixPath ),
			                                                   new RequestMethodsRequestCondition(),
			                                                   new ParamsRequestCondition(),
			                                                   new HeadersRequestCondition(),
			                                                   new ConsumesRequestCondition(),
			                                                   new ProducesRequestCondition(),
			                                                   getCustomMethodCondition( method ) );

			info = other.combine( info );
		}

		return info;
	}

	@Override
	protected RequestCondition<?> getCustomMethodCondition( Method method ) {
		CustomRequestCondition methodAnnotation = AnnotationUtils.findAnnotation( method, CustomRequestCondition.class );
		if( methodAnnotation != null ) {
			Class<? extends CustomRequestConditionMatcher>[] conditions = methodAnnotation.conditions();
			Collection<CustomRequestConditionMatcher> instances = new ArrayList<>();
			for( Class<? extends CustomRequestConditionMatcher> condition : conditions ) {
				instances.add( BeanUtils.instantiateClass( condition ) );
			}
			return new CustomRequestConditions( instances );
		}
		return super.getCustomMethodCondition( method );
	}

}
