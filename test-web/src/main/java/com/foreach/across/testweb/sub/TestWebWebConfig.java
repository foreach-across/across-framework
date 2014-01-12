package com.foreach.across.testweb.sub;

import com.foreach.across.modules.web.ui.AbstractWebUiContextHandlerInterceptor;
import com.foreach.across.testweb.SpecificUiContext;
import com.foreach.across.testweb.SpecificUiContextImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class TestWebWebConfig extends WebMvcConfigurationSupport
{
	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping mapping = super.requestMappingHandlerMapping();
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
			protected SpecificUiContext createWebUiContext(
					HttpServletRequest request, HttpServletResponse response, MessageSource messageSource ) {
				return new SpecificUiContextImpl( request, response );
			}
		};
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
		engine.setTemplateResolver( templateResolver() );

		return engine;
	}

	@Bean
	public ThymeleafViewResolver thymeleafViewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine( springTemplateEngine() );

		//resolver.setViewNames( new String[] { "*.thtml" } );

		return resolver;
	}

	/*
		public SpecificUiContextFactory uiContextFactory() {
			return new SpecificUiContextFactory();
		}
	*/
	@Bean
	public TemplateResolver templateResolver() {
		TemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setTemplateMode( "HTML5" );
		resolver.setPrefix( "classpath:/views/" );
		resolver.setSuffix( ".thtml" );

		return resolver;
	}
}
