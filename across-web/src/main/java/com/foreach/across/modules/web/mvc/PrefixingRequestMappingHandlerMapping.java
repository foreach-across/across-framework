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
import com.foreach.across.modules.web.mvc.condition.CompositeCustomRequestCondition;
import com.foreach.across.modules.web.mvc.condition.CustomRequestCondition;
import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.ClassFilter;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans matching beans for RequestMapping annotations and (optionally) prefixes all mappings.
 * Allows for reloading (re-scanning) of mappings and re-initialization of the entire mapping handler mapping.
 * <p>
 * <b>WARN: interceptors are only supported once.</b>
 * <p/>
 * Since 2.0.0 also supports {@link CustomRequestMapping} annotations on handler methods.
 * Any {@link CustomRequestCondition} will be created using the {@link AutowireCapableBeanFactory} of the attached
 * {@link ApplicationContext}.  Note it will be created as a new prototype bean, existing beans of that type will
 * be ignored.
 *
 * @see CustomRequestMapping
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

		Set<Method> methods = MethodIntrospector.selectMethods(
				userType,
				(ReflectionUtils.MethodFilter) method -> getMappingForMethod( method, userType ) != null
		);

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
		RequestMappingInfo info = createRequestMappingInfo( method );
		if ( info != null ) {
			RequestMappingInfo typeInfo = createRequestMappingInfo( handlerType );
			if ( typeInfo != null ) {
				info = typeInfo.combine( info );
			}
		}

		if ( info != null && prefixPath != null ) {
			RequestMappingInfo other = new RequestMappingInfo( new PatternsRequestCondition( prefixPath ),
			                                                   new RequestMethodsRequestCondition(),
			                                                   new ParamsRequestCondition(),
			                                                   new HeadersRequestCondition(),
			                                                   new ConsumesRequestCondition(),
			                                                   new ProducesRequestCondition(),
			                                                   new CompositeCustomRequestCondition() );

			info = other.combine( info );
		}

		return info;
	}

	// Replaced so a request mapping that is composed only be a custom condition can be returned
	private RequestMappingInfo createRequestMappingInfo( AnnotatedElement element ) {
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation( element, RequestMapping.class );
		RequestCondition<?> condition = ( element instanceof Class ?
				getCustomTypeCondition( (Class<?>) element ) : getCustomMethodCondition( (Method) element ) );

		return createRequestMappingInfo( requestMapping, condition );
	}

	@Override
	protected RequestMappingInfo createRequestMappingInfo( RequestMapping requestMapping,
	                                                       RequestCondition<?> customCondition ) {
		if ( requestMapping == null && customCondition == null ) {
			return null;
		}

		if ( requestMapping == null ) {
			requestMapping = AnnotatedElementUtils.findMergedAnnotation( Wildcard.class, RequestMapping.class );
		}

		return super.createRequestMappingInfo( requestMapping, customCondition );
	}

	@Override
	protected RequestCondition<?> getCustomTypeCondition( Class<?> handlerType ) {
		return buildCustomRequestCondition( handlerType );
	}

	@Override
	protected RequestCondition<?> getCustomMethodCondition( Method method ) {
		return buildCustomRequestCondition( method );
	}

	private RequestCondition<?> buildCustomRequestCondition( AnnotatedElement annotatedElement ) {
		AutowireCapableBeanFactory beanFactory = getApplicationContext().getAutowireCapableBeanFactory();

		List<CustomRequestCondition> conditions = getCustomRequestConditionClasses( annotatedElement )
				.stream()
				.flatMap( Stream::of )
				.distinct()
				.map( c -> {
					CustomRequestCondition condition = beanFactory.createBean( c );
					condition.setAnnotatedElement( annotatedElement );
					return condition;
				} )
				.collect( Collectors.toList() );

		return conditions.isEmpty() ? null : new CompositeCustomRequestCondition( conditions );
	}

	@SuppressWarnings("unchecked")
	private List<Class<CustomRequestCondition>[]> getCustomRequestConditionClasses( AnnotatedElement annotatedElement ) {
		MultiValueMap<String, Object> attributes = AnnotatedElementUtils.getAllAnnotationAttributes(
				annotatedElement, CustomRequestMapping.class.getName(), false, true
		);

		Object values = ( attributes != null ? attributes.get( "value" ) : null );
		return (List<Class<CustomRequestCondition>[]>) ( values != null ? values : Collections.emptyList() );
	}

	// placeholder for the wildcard @RequestMapping
	@RequestMapping
	private static final class Wildcard
	{
	}
}
