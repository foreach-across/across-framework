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

package com.foreach.across.modules.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.*;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.convert.StringToDateConverter;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurer;
import com.foreach.across.modules.web.context.PrefixingPathRegistry;
import com.foreach.across.modules.web.mvc.*;
import com.foreach.across.modules.web.template.LayoutingExceptionHandlerExceptionResolver;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import java.util.*;

/**
 * Default AcrossWeb web mvc configuration: creates an adapter and handler mapping, and will
 * apply all WebMvcConfigurer components when the context is bootstrapped.
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 */
@Configuration
@AcrossEventHandler
public class AcrossWebDefaultMvcConfiguration implements ApplicationContextAware, ServletContextAware
{
	private static final boolean jaxb2Present =
			ClassUtils.isPresent( "javax.xml.bind.Binder", WebMvcConfigurationSupport.class.getClassLoader() );
	private static final boolean jackson2Present =
			ClassUtils.isPresent( "com.fasterxml.jackson.databind.ObjectMapper",
			                      WebMvcConfigurationSupport.class.getClassLoader() ) &&
					ClassUtils.isPresent( "com.fasterxml.jackson.core.JsonGenerator",
					                      WebMvcConfigurationSupport.class.getClassLoader() );
	private static final boolean jackson2XmlPresent =
			ClassUtils.isPresent( "com.fasterxml.jackson.dataformat.xml.XmlMapper",
			                      WebMvcConfigurationSupport.class.getClassLoader() );
	private static final boolean gsonPresent =
			ClassUtils.isPresent( "com.google.gson.Gson", WebMvcConfigurationSupport.class.getClassLoader() );
	private static final boolean romePresent =
			ClassUtils.isPresent( "com.rometools.rome.feed.WireFeed",
			                      WebMvcConfigurationSupport.class.getClassLoader() );

	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebDefaultMvcConfiguration.class );

	@Autowired
	@Qualifier(AcrossContext.BEAN)
	private AcrossContext acrossContext;

	@Autowired
	@Module(AcrossModule.CURRENT_MODULE)
	private AcrossModuleInfo currentModuleInfo;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@RefreshableCollection(includeModuleInternals = true)
	private Collection<PrefixingHandlerMappingConfigurer> prefixingHandlerMappingConfigurers;

	@RefreshableCollection(includeModuleInternals = true)
	private Collection<WebMvcConfigurer> webMvcConfigurers;

	private ApplicationContext applicationContext;

	private ServletContext servletContext;

	@Autowired(required = false)
	@Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN)
	private FormattingConversionService mvcConversionService;

	@Autowired(required = false)
	private WebTemplateInterceptor webTemplateInterceptor;

	@Autowired(required=false)
	private ResourceUrlProviderExposingInterceptor resourceUrlProviderExposingInterceptor;

	@Autowired(required = false)
	private ResourceUrlProvider resourceUrlProvider;

	private ConfigurableWebBindingInitializer initializer;

	private ValidatorDelegate validatorDelegate = new ValidatorDelegate();

	public void setServletContext( ServletContext servletContext ) {
		this.servletContext = servletContext;
	}

	public void setApplicationContext( ApplicationContext applicationContext ) {
		this.applicationContext = applicationContext;
	}

	@PostConstruct
	protected void validateServletContext() {
		Assert.notNull( applicationContext, "applicationContext should be autowired and cannot be null" );
		Assert.notNull( servletContext, "servletContext should be autowired and cannot be null" );
		Assert.notNull( acrossContext );

		if ( mvcConversionService == null ) {
			mvcConversionService = mvcConversionService();
		}
	}

	@Bean(name = AcrossWebModule.CONVERSION_SERVICE_BEAN)
	@Exposed
	@AcrossCondition("not hasBean('" + AcrossWebModule.CONVERSION_SERVICE_BEAN + "', T(org.springframework.format.support.FormattingConversionService))")
	public FormattingConversionService mvcConversionService() {
		if ( beanRegistry.containsBean( ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME ) ) {
			Object conversionService
					= beanRegistry.getBean( ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME );

			if ( conversionService instanceof FormattingConversionService ) {
				LOG.info( "Using the default ConversionService as {}", AcrossWebModule.CONVERSION_SERVICE_BEAN );
				return (FormattingConversionService) conversionService;
			}
		}

		LOG.info(
				"No ConversionService named {} found in Across context - creating and exposing a new FormattingConversionService bean",
				AcrossWebModule.CONVERSION_SERVICE_BEAN );

		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addConverter( new StringToDateConverter() );

		return conversionService;
	}

	/**
	 * Reload the configuration by applying all WebMvcConfigurers in the context.
	 */
	@Event
	protected void reload( AcrossContextBootstrappedEvent bootstrappedEvent ) {
		// Reload the adapter
		List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<HandlerMethodReturnValueHandler>();
		List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();

		InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
		ContentNegotiationConfigurer contentNegotiationConfigurer = new ContentNegotiationConfigurer( servletContext );
		contentNegotiationConfigurer.mediaTypes( getDefaultMediaTypes() );

		ResourceHandlerRegistry resourceHandlerRegistry =
				new ResourceHandlerRegistry( applicationContext, servletContext, currentModuleInfo );

		DelayedAsyncSupportConfigurer asyncSupportConfigurer = new DelayedAsyncSupportConfigurer();

		for ( WebMvcConfigurer configurer : webMvcConfigurers ) {
			configurer.addArgumentResolvers( argumentResolvers );
			configurer.addReturnValueHandlers( returnValueHandlers );
			configurer.configureMessageConverters( messageConverters );
			configurer.addInterceptors( interceptorRegistry );
			configurer.configureContentNegotiation( contentNegotiationConfigurer );
			configurer.addResourceHandlers( resourceHandlerRegistry );
			configurer.addFormatters( mvcConversionService );
			configurer.configureHandlerExceptionResolvers( exceptionResolvers );
			configurer.configureAsyncSupport( asyncSupportConfigurer );
		}

		for ( PrefixingHandlerMappingConfigurer configurer : prefixingHandlerMappingConfigurers ) {
			if ( configurer.supports( AcrossWebModule.NAME ) ) {
				configurer.addInterceptors( interceptorRegistry );
			}
		}

		//if ( messageConverters.isEmpty() ) {
		addDefaultHttpMessageConverters( messageConverters );
		//}

		ContentNegotiationManager contentNegotiationManager;

		try {
			contentNegotiationManager = contentNegotiationConfigurer.getContentNegotiationManager();
		}
		catch ( Exception e ) {
			LOG.error( "Could not create ContentNegotiationManager", e );
			throw new BeanInitializationException( "Could not create ContentNegotiationManager", e );
		}

		ReloadableRequestMappingHandlerAdapter adapter = requestMappingHandlerAdapter();
		adapter.setContentNegotiationManager( contentNegotiationManager );
		if ( !messageConverters.isEmpty() ) {
			adapter.setMessageConverters( messageConverters );
		}
		adapter.setCustomArgumentResolvers( argumentResolvers );
		adapter.setCustomReturnValueHandlers( returnValueHandlers );

		// Async support
		if ( asyncSupportConfigurer.getTaskExecutor() != null ) {
			adapter.setTaskExecutor( asyncSupportConfigurer.getTaskExecutor() );
		}
		if ( asyncSupportConfigurer.getTimeout() != null ) {
			adapter.setAsyncRequestTimeout( asyncSupportConfigurer.getTimeout() );
		}
		adapter.setCallableInterceptors( asyncSupportConfigurer.getCallableInterceptors() );
		adapter.setDeferredResultInterceptors( asyncSupportConfigurer.getDeferredResultInterceptors() );

		adapter.reload();

		// Update the controller mapping
		PrefixingRequestMappingHandlerMapping controllerHandlerMapping = controllerHandlerMapping();
		controllerHandlerMapping.setContentNegotiationManager( contentNegotiationManager );
		controllerHandlerMapping.setInterceptors( interceptorRegistry.getInterceptors().toArray() );

		controllerHandlerMapping.reload();

		// Update the resource handler mapping
		SimpleUrlHandlerMapping resourceHandlerMapping = resourceHandlerMapping();
		if( resourceUrlProviderExposingInterceptor != null && resourceUrlProvider != null ) {
			resourceHandlerMapping.setInterceptors(new HandlerInterceptor[] {resourceUrlProviderExposingInterceptor});
		}
		resourceHandlerMapping.setUrlMap( resourceHandlerRegistry.getUrlMap() );
		resourceHandlerMapping.initApplicationContext();

//		// Detect the handler mappings
		if( resourceUrlProvider != null ) {
			resourceUrlProvider.onApplicationEvent( new ContextRefreshedEvent( applicationContext ) );
		}

		// Handler exception resolver
		if ( exceptionResolvers.isEmpty() ) {
			addDefaultHandlerExceptionResolvers( exceptionResolvers, contentNegotiationManager, messageConverters );
		}

		HandlerExceptionResolverComposite handlerExceptionResolver = handlerExceptionResolver();
		handlerExceptionResolver.setExceptionResolvers( exceptionResolvers );

		// Uri components contributor
		CompositeUriComponentsContributor mvcUriComponentsContributor = mvcUriComponentsContributor();
		mvcUriComponentsContributor.setContributors( adapter.getArgumentResolvers() );

		// Set the validator
		Validator validator = getValidator( webMvcConfigurers );
		setValidator( validator );

		// Set the message codes resolver
		MessageCodesResolver resolver = getMessageCodesResolver( webMvcConfigurers );

		if ( resolver != null ) {
			getConfigurableWebBindingInitializer().setMessageCodesResolver( resolver );
		}
	}

	public Validator getValidator( Collection<WebMvcConfigurer> delegates ) {
		List<Validator> candidates = new ArrayList<Validator>();
		for ( WebMvcConfigurer configurer : delegates ) {
			Validator validator = configurer.getValidator();
			if ( validator != null ) {
				candidates.add( validator );
			}
		}
		return selectSingleInstance( candidates, Validator.class );
	}

	private MessageCodesResolver getMessageCodesResolver( Collection<WebMvcConfigurer> delegates ) {
		List<MessageCodesResolver> candidates = new ArrayList<MessageCodesResolver>();
		for ( WebMvcConfigurer configurer : delegates ) {
			MessageCodesResolver messageCodesResolver = configurer.getMessageCodesResolver();
			if ( messageCodesResolver != null ) {
				candidates.add( messageCodesResolver );
			}
		}
		return selectSingleInstance( candidates, MessageCodesResolver.class );
	}

	private <T> T selectSingleInstance( List<T> instances, Class<T> instanceType ) {
		if ( instances.size() > 1 ) {
			throw new IllegalStateException(
					"Only one [" + instanceType + "] was expected but multiple instances were provided: " + instances );
		}
		else if ( instances.size() == 1 ) {
			return instances.get( 0 );
		}
		else {
			return null;
		}
	}

	protected Map<String, MediaType> getDefaultMediaTypes() {
		Map<String, MediaType> map = new HashMap<String, MediaType>();
		if ( romePresent ) {
			map.put( "atom", MediaType.APPLICATION_ATOM_XML );
			map.put( "rss", MediaType.valueOf( "application/rss+xml" ) );
		}
		if ( jackson2Present || gsonPresent ) {
			map.put( "json", MediaType.APPLICATION_JSON );
		}
		if ( jaxb2Present || jackson2XmlPresent ) {
			map.put( "xml", MediaType.APPLICATION_XML );
		}
		return map;
	}

	@Bean
	@Exposed
	public CompositeUriComponentsContributor mvcUriComponentsContributor() {
		return new CompositeUriComponentsContributor( mvcConversionService );
	}

	/**
	 * Return the {@link org.springframework.web.bind.support.ConfigurableWebBindingInitializer} to use for
	 * initializing all {@link org.springframework.web.bind.WebDataBinder} instances.
	 */
	private ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
		if ( initializer == null ) {
			initializer = new ConfigurableWebBindingInitializer();
			initializer.setConversionService( mvcConversionService );
			initializer.setValidator( mvcValidator() );
		}

		return initializer;
	}

	/**
	 * Adds a set of default HttpMessageConverter instances to the given list.
	 *
	 * @param messageConverters the list to add the default message converters to
	 */
	@SuppressWarnings("deprecation")
	protected final void addDefaultHttpMessageConverters( List<HttpMessageConverter<?>> messageConverters ) {
		// Todo: write a custom AcrossWebConfigurer configurer
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setWriteAcceptCharset( false );

		addIfNoInstanceYetPresent( messageConverters, new ByteArrayHttpMessageConverter() );
		addIfNoInstanceYetPresent( messageConverters, stringConverter );
		addIfNoInstanceYetPresent( messageConverters, new ResourceHttpMessageConverter() );
		addIfNoInstanceYetPresent( messageConverters, new SourceHttpMessageConverter<Source>() );
		addIfNoInstanceYetPresent( messageConverters, new AllEncompassingFormHttpMessageConverter() );

		if ( romePresent ) {
			addIfNoInstanceYetPresent( messageConverters, new AtomFeedHttpMessageConverter() );
			addIfNoInstanceYetPresent( messageConverters, new RssChannelHttpMessageConverter() );
		}
		if ( jackson2XmlPresent ) {
			ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.xml().applicationContext( this.applicationContext )
			                                                       .build();
			addIfNoInstanceYetPresent( messageConverters, new MappingJackson2XmlHttpMessageConverter( objectMapper ) );
		}
		else if ( jaxb2Present ) {
			addIfNoInstanceYetPresent( messageConverters, new Jaxb2RootElementHttpMessageConverter() );
		}
		if ( jackson2Present ) {
			ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext( this.applicationContext )
			                                                       .build();
			addIfNoInstanceYetPresent( messageConverters, new MappingJackson2HttpMessageConverter( objectMapper ) );
		}
		else if ( gsonPresent ) {
			messageConverters.add( new GsonHttpMessageConverter() );
		}
	}

	private void addIfNoInstanceYetPresent( List<HttpMessageConverter<?>> messageConverters,
	                                        HttpMessageConverter<?> converter ) {
		boolean found = false;

		for ( HttpMessageConverter current : messageConverters ) {
			if ( converter.getClass().isAssignableFrom( current.getClass() ) ) {
				found = true;
			}
		}

		if ( !found ) {
			messageConverters.add( converter );
		}
	}

	@Bean
	@Exposed
	public Validator mvcValidator() {
		return validatorDelegate;
	}

	private void setValidator( Validator implementation ) {
		Validator validator = implementation;
		if ( validator == null ) {
			if ( ClassUtils.isPresent( "javax.validation.Validator", getClass().getClassLoader() ) ) {
				Class<?> clazz;
				try {
					String className = "org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean";
					clazz = ClassUtils.forName( className, WebMvcConfigurationSupport.class.getClassLoader() );
				}
				catch ( ClassNotFoundException | LinkageError e ) {
					throw new BeanInitializationException( "Could not find default validator", e );
				}

				validator = (Validator) applicationContext.getAutowireCapableBeanFactory().createBean( clazz );
			}
			else {
				validator = new Validator()
				{
					public boolean supports( Class<?> clazz ) {
						return false;
					}

					public void validate( Object target, Errors errors ) {
					}
				};
			}
		}

		validatorDelegate.setImplementation( validator );
	}

	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping controllerHandlerMapping() {
		PrefixingRequestMappingHandlerMapping handlerMapping =
				new PrefixingRequestMappingHandlerMapping( new AnnotationClassFilter( Controller.class, true ) );
		handlerMapping.setOrder( currentModuleInfo.getIndex() );

		return handlerMapping;
	}

	@Bean
	@Exposed
	public PrefixingPathRegistry prefixingPathRegistry() {
		return new PrefixingPathRegistry();
	}

	@Bean
	@Exposed
	public SimpleUrlHandlerMapping resourceHandlerMapping() {
		return new SimpleUrlHandlerMapping();
	}

	@Bean
	@Exposed
	public ReloadableRequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		ReloadableRequestMappingHandlerAdapter adapter = new ReloadableRequestMappingHandlerAdapter();
		adapter.setWebBindingInitializer( getConfigurableWebBindingInitializer() );
		return adapter;
	}

	@Bean
	@Exposed
	public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
		return new HttpRequestHandlerAdapter();
	}

	@Bean
	@Exposed
	public HandlerExceptionResolverComposite handlerExceptionResolver() {
		HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
		composite.setOrder( currentModuleInfo.getIndex() );
		return composite;
	}

	private void addDefaultHandlerExceptionResolvers( List<HandlerExceptionResolver> exceptionResolvers,
	                                                  ContentNegotiationManager contentNegotiationManager,
	                                                  List<HttpMessageConverter<?>> messageConverters ) {
		ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver;
		if ( webTemplateInterceptor != null ) {
			exceptionHandlerExceptionResolver =
					new LayoutingExceptionHandlerExceptionResolver( webTemplateInterceptor );

		}
		else {
			exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
		}
		exceptionHandlerExceptionResolver.setApplicationContext( this.applicationContext );
		exceptionHandlerExceptionResolver.setContentNegotiationManager( contentNegotiationManager );
		if ( !messageConverters.isEmpty() ) {
			exceptionHandlerExceptionResolver.setMessageConverters( messageConverters );
		}
		exceptionHandlerExceptionResolver.afterPropertiesSet();

		exceptionResolvers.add( exceptionHandlerExceptionResolver );
		exceptionResolvers.add( new ResponseStatusExceptionResolver() );
		exceptionResolvers.add( new DefaultHandlerExceptionResolver() );
	}

	/**
	 * Inherited in order to expose properties.
	 */
	final static class DelayedAsyncSupportConfigurer extends AsyncSupportConfigurer
	{
		@Override
		protected AsyncTaskExecutor getTaskExecutor() {
			return super.getTaskExecutor();
		}

		@Override
		protected Long getTimeout() {
			return super.getTimeout();
		}

		@Override
		protected List<CallableProcessingInterceptor> getCallableInterceptors() {
			return super.getCallableInterceptors();
		}

		@Override
		protected List<DeferredResultProcessingInterceptor> getDeferredResultInterceptors() {
			return super.getDeferredResultInterceptors();
		}
	}
}
