package com.foreach.across.testweb.sub;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.ui.AbstractWebUiContextHandlerInterceptor;
import com.foreach.across.testweb.SpecificUiContext;
import com.foreach.across.testweb.SpecificUiContextImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Exposed
@Configuration
public class TestWebWebConfig extends WebMvcConfigurationSupport
{
	@Autowired
	private List<HandlerMethodArgumentResolver> argumentResolvers;

	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping mapping = super.requestMappingHandlerMapping();
		mapping.setDetectHandlerMethodsInAncestorContexts( true );

		return mapping;
	}

	@Bean
	@Override
	public HandlerMapping resourceHandlerMapping() {
		return super.resourceHandlerMapping();
	}

	@Override
	protected void addResourceHandlers( ResourceHandlerRegistry registry ) {
		registry.addResourceHandler( "/static/**" ).addResourceLocations( "classpath:/views/" );

		registry.addResourceHandler( "/static/css/ehcache/**" ).addResourceLocations(
				new File( "c:/code/across/across-ehcache/src/main/resources/views/css/ehcache" ).toURI().toString() );
	}

	@Override
	protected void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( contextInterceptorHandler() );
	}

	/*
	@Bean
	public WebResourceRegistryInterceptor resourceRegistryInterceptor() {
		WebResourceRegistryInterceptor interceptor = new WebResourceRegistryInterceptor();

		WebResourceRegistry registry = new WebResourceRegistry();
		registry.addWithKey( WebResource.CSS, DebugWebConfig.RESOURCE_KEY, "/css/debugweb/debugweb.css",
		                     WebResource.VIEWS );

		interceptor.setDefaultRegistry( registry );

		interceptor.addWebResourceTranslator( new WebResourceTranslator()
		{
			public boolean shouldTranslate( WebResource resource ) {
				return StringUtils.equals( WebResource.VIEWS, resource.getLocation() );
			}

			public void translate( WebResource resource ) {
				resource.setLocation( WebResource.RELATIVE );
				resource.setData( "/static" + resource.getData() );
			}
		} );

		return interceptor;
	}
	*/

	@Bean
	public AbstractWebUiContextHandlerInterceptor contextInterceptorHandler() {
		return new AbstractWebUiContextHandlerInterceptor<SpecificUiContext>()
		{
			@Override
			protected SpecificUiContext createWebUiContext( HttpServletRequest request,
			                                                HttpServletResponse response,
			                                                MessageSource messageSource ) {
				return new SpecificUiContextImpl( request, response );
			}
		};
	}

	@Override
	protected void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.addAll( this.argumentResolvers );
	}

	/*
	@Bean
	public UrlBasedViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass( InternalResourceView.class );
		viewResolver.setPrefix( "/views/" );
		viewResolver.setRedirectContextRelative( true );
		viewResolver.setSuffix( ".jsp" );
		return viewResolver;
	}
	*/

	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();

		Set a = new LinkedHashSet();
		a.add( templateResolver3() );
		a.add( templateResolver2() );
		a.add( templateResolver() );

		engine.setTemplateResolvers( a );

		return engine;
	}

	@Bean
	public ViewResolver jstlViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix( "/WEB-INF/jsp/" );
		resolver.setSuffix( ".jsp" );
		resolver.setOrder( 2 );
		return resolver;
	}

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine() );
		resolver.setOrder( 1 );
		resolver.setViewNames( new String[] { "th/*" } );

		return resolver;
	}

	/*
		public SpecificUiContextFactory uiContextFactory() {
			return new SpecificUiContextFactory();
		}
	*/

	@Bean
	public TemplateResolver templateResolver2() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( false );
		resolver.setPrefix( "file:/code/across/debug-web/src/main/resources/views/" );
		resolver.setSuffix( ".thtml" );

		return resolver;
	}

	@Bean
	public TemplateResolver templateResolver3() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( false );
		resolver.setPrefix( "file:/code/across/across-ehcache/src/main/resources/views/" );
		resolver.setSuffix( ".thtml" );

		return resolver;
	}

	@Bean
	public TemplateResolver templateResolver() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( false );
		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );

		return resolver;
	}
}
