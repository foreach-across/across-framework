package com.foreach.across.testweb.sub;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.debugweb.mvc.DebugHandlerMapping;
import com.foreach.across.modules.debugweb.mvc.DebugPageViewArgumentResolver;
import com.foreach.across.modules.web.ui.AbstractWebUiContextHandlerInterceptor;
import com.foreach.across.testweb.SpecificUiContext;
import com.foreach.across.testweb.SpecificUiContextImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Exposed
@Configuration
public class TestWebWebConfig extends WebMvcConfigurationSupport
{
	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping mapping = super.requestMappingHandlerMapping();
		mapping.setDetectHandlerMethodsInAncestorContexts( true );

		return mapping;
	}

	@Bean
	public DebugHandlerMapping debugHandlerMapping() {
		DebugHandlerMapping mapping = new DebugHandlerMapping();
		mapping.setDetectHandlerMethodsInAncestorContexts( true );

		return mapping;
	}

	@Override
	protected void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( contextInterceptorHandler() );
	}

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

	@Autowired
	private DebugPageViewArgumentResolver debugPageViewArgumentResolver;

	@Override
	protected void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.add( debugPageViewArgumentResolver );
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
		//resolver.setViewNames( new String[] { "*.thtml" } );

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
	public TemplateResolver templateResolver() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setCacheable( false );
		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );

		return resolver;
	}
}
