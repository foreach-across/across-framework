package com.foreach.across.modules.web.config;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.registry.RefreshableRegistry;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.*;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.ConversionServiceExposingInterceptor;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
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

	private static final boolean jackson2Present = ClassUtils.isPresent( "com.fasterxml.jackson.databind.ObjectMapper",
	                                                                     WebMvcConfigurationSupport.class.getClassLoader() ) && ClassUtils.isPresent(
			"com.fasterxml.jackson.core.JsonGenerator", WebMvcConfigurationSupport.class.getClassLoader() );

	private static final boolean jacksonPresent = ClassUtils.isPresent( "org.codehaus.jackson.map.ObjectMapper",
	                                                                    WebMvcConfigurationSupport.class.getClassLoader() ) && ClassUtils.isPresent(
			"org.codehaus.jackson.JsonGenerator", WebMvcConfigurationSupport.class.getClassLoader() );

	private static boolean romePresent = ClassUtils.isPresent( "com.sun.syndication.feed.WireFeed",
	                                                           WebMvcConfigurationSupport.class.getClassLoader() );

	private static final Logger LOG = LoggerFactory.getLogger( AcrossWebDefaultMvcConfiguration.class );

	@Autowired
	@Qualifier(AcrossContext.BEAN)
	private AcrossContext acrossContext;

	@Autowired
	@Qualifier(AcrossModule.CURRENT_MODULE)
	private AcrossWebModule webModule;

	private ApplicationContext applicationContext;

	private ServletContext servletContext;

	private FormattingConversionService existingConversionService;

	public void setServletContext( ServletContext servletContext ) {
		this.servletContext = servletContext;
	}

	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@PostConstruct
	private void validateServletContext() {
		Assert.notNull( applicationContext, "applicationContext should be autowired and cannot be null" );
		Assert.notNull( servletContext, "servletContext should be autowired and cannot be null" );
		Assert.notNull( acrossContext );
		Assert.notNull( webModule );

		Collection<FormattingConversionService> existing =
				AcrossContextUtils.getBeansOfType( acrossContext, FormattingConversionService.class );

		if ( !existing.isEmpty() ) {
			existingConversionService = existing.iterator().next();
		}
	}

	/**
	 * Apply all WebMvcConfigurers in the context and reload the
	 */
	@Handler
	private void reload( AcrossContextBootstrappedEvent bootstrappedEvent ) {
		RefreshableRegistry<WebMvcConfigurer> webMvcConfigurers = webMvcConfigurers();
		webMvcConfigurers.refresh();

		// Reload the adapter
		List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<HandlerMethodReturnValueHandler>();
		List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<HandlerExceptionResolver>();

		InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
		ContentNegotiationConfigurer contentNegotiationConfigurer = new ContentNegotiationConfigurer( servletContext );
		contentNegotiationConfigurer.mediaTypes( getDefaultMediaTypes() );

		ResourceHandlerRegistry resourceHandlerRegistry =
				new ResourceHandlerRegistry( applicationContext, servletContext );
		FormattingConversionService conversionService = mvcConversionService();

		for ( WebMvcConfigurer configurer : webMvcConfigurers ) {
			configurer.addArgumentResolvers( argumentResolvers );
			configurer.addReturnValueHandlers( returnValueHandlers );
			configurer.configureMessageConverters( messageConverters );
			configurer.addInterceptors( interceptorRegistry );
			configurer.configureContentNegotiation( contentNegotiationConfigurer );
			configurer.addResourceHandlers( resourceHandlerRegistry );
			configurer.addFormatters( conversionService );
			configurer.configureHandlerExceptionResolvers( exceptionResolvers );
		}

		interceptorRegistry.addInterceptor( new ConversionServiceExposingInterceptor( conversionService ) );

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

		adapter.reload();

		// Update the controller mapping
		PrefixingRequestMappingHandlerMapping controllerHandlerMapping = controllerHandlerMapping();
		controllerHandlerMapping.setContentNegotiationManager( contentNegotiationManager );
		controllerHandlerMapping.setInterceptors( interceptorRegistry.getInterceptors().toArray() );

		controllerHandlerMapping.reload();

		// Update the resource handler mapping
		SimpleUrlHandlerMapping resourceHandlerMapping = resourceHandlerMapping();
		resourceHandlerMapping.setUrlMap( resourceHandlerRegistry.getUrlMap() );
		resourceHandlerMapping.initApplicationContext();

		// Handler exception resolver
		if ( exceptionResolvers.isEmpty() ) {
			addDefaultHandlerExceptionResolvers( exceptionResolvers, contentNegotiationManager );
		}

		HandlerExceptionResolverComposite handlerExceptionResolver = handlerExceptionResolver();
		handlerExceptionResolver.setExceptionResolvers( exceptionResolvers );

		// Uri components contributor
		CompositeUriComponentsContributor mvcUriComponentsContributor = mvcUriComponentsContributor();
		mvcUriComponentsContributor.setContributors( adapter.getArgumentResolvers() );
	}

	protected Map<String, MediaType> getDefaultMediaTypes() {
		Map<String, MediaType> map = new HashMap<String, MediaType>();
		if ( romePresent ) {
			map.put( "atom", MediaType.APPLICATION_ATOM_XML );
			map.put( "rss", MediaType.valueOf( "application/rss+xml" ) );
		}
		if ( jackson2Present || jacksonPresent ) {
			map.put( "json", MediaType.APPLICATION_JSON );
		}
		if ( jaxb2Present ) {
			map.put( "xml", MediaType.APPLICATION_XML );
		}
		return map;
	}

	@Bean
	@Exposed
	public CompositeUriComponentsContributor mvcUriComponentsContributor() {
		return new CompositeUriComponentsContributor( mvcConversionService() );
	}

	@Bean
	@Exposed
	public FormattingConversionService mvcConversionService() {
		if ( existingConversionService != null ) {
			return existingConversionService;
		}

		return new DefaultFormattingConversionService();
	}

	/**
	 * Return the {@link org.springframework.web.bind.support.ConfigurableWebBindingInitializer} to use for
	 * initializing all {@link org.springframework.web.bind.WebDataBinder} instances.
	 */
	protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
		ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
		initializer.setConversionService( mvcConversionService() );
		initializer.setValidator( mvcValidator() );
		//initializer.setMessageCodesResolver( getMessageCodesResolver() );
		return initializer;
	}

	@Bean
	@Exposed
	public Validator mvcValidator() {
		Validator validator = null;
		if ( validator == null ) {
			if ( ClassUtils.isPresent( "javax.validation.Validator", getClass().getClassLoader() ) ) {
				Class<?> clazz;
				try {
					String className = "org.springframework.validation.beanvalidation.LocalValidatorFactoryBean";
					clazz = ClassUtils.forName( className, WebMvcConfigurationSupport.class.getClassLoader() );
				}
				catch ( ClassNotFoundException e ) {
					throw new BeanInitializationException( "Could not find default validator", e );
				}
				catch ( LinkageError e ) {
					throw new BeanInitializationException( "Could not find default validator", e );
				}
				validator = (Validator) BeanUtils.instantiate( clazz );
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
		return validator;
	}

	@Bean
	protected RefreshableRegistry<WebMvcConfigurer> webMvcConfigurers() {
		return new RefreshableRegistry<WebMvcConfigurer>( WebMvcConfigurer.class, true );
	}

	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping controllerHandlerMapping() {
		PrefixingRequestMappingHandlerMapping handlerMapping =
				new PrefixingRequestMappingHandlerMapping( new AnnotationClassFilter( Controller.class ) );
		handlerMapping.setOrder( 0 );

		return handlerMapping;
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

		AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
		//configureAsyncSupport(configurer);

		/*
		if (configurer.getTaskExecutor() != null) {
			adapter.setTaskExecutor(configurer.getTaskExecutor());
		}
		if (configurer.getTimeout() != null) {
			adapter.setAsyncRequestTimeout(configurer.getTimeout());
		}
		adapter.setCallableInterceptors(configurer.getCallableInterceptors());
		adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());
		*/

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
		composite.setOrder( 0 );
		return composite;
	}

	private void addDefaultHandlerExceptionResolvers( List<HandlerExceptionResolver> exceptionResolvers,
	                                                  ContentNegotiationManager contentNegotiationManager ) {
		ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
		exceptionHandlerExceptionResolver.setApplicationContext( this.applicationContext );
		exceptionHandlerExceptionResolver.setContentNegotiationManager( contentNegotiationManager );
		//exceptionHandlerExceptionResolver.setMessageConverters( getMessageConverters() );
		exceptionHandlerExceptionResolver.afterPropertiesSet();

		exceptionResolvers.add( exceptionHandlerExceptionResolver );
		exceptionResolvers.add( new ResponseStatusExceptionResolver() );
		exceptionResolvers.add( new DefaultHandlerExceptionResolver() );
	}
}